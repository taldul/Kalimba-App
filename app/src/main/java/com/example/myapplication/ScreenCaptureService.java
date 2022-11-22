package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenCaptureService extends Service {
    private ServiceHandler handler;
    private Intent pData;

    public ScreenCaptureService() {
        HandlerThread thread = new HandlerThread("handler thread");
        thread.start();

        MediaProjection p = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).getMediaProjection(-1, pData);
        handler = new ServiceHandler(this, thread.getLooper(), p);
    }

    private static class ServiceHandler extends Handler {

        private MediaCodec encoder;
        private MediaMuxer muxer;
        private VirtualDisplay virtualDisplay;
        private MediaProjection mediaProj;

        private final String mimetype = MediaFormat.MIMETYPE_VIDEO_MPEG4;
        private final String audioUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Recorder video.mp4";
        private final int frameRate = 30;
        private final int bitRate = 6 * (int) Math.pow(2, 20) * 8;
        private final int keyFrameFreq = 2;
        private final int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

        private boolean isRecording;

        private final  ScreenCaptureService s;

        public ServiceHandler (ScreenCaptureService service, Looper looper, final MediaProjection p) {
            super(looper);
            s = service;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                //start audio capturing
                case 0: {
                    //create the encoder and muxer object
                    try {
                        encoder = MediaCodec.createEncoderByType(mimetype);
                        muxer = new MediaMuxer(audioUrl, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // set callback to run encoder  in async mode
                    encoder.setCallback(new MediaCodec.Callback() {

                        private String TAG = "encoder_callback";
                        int track;
                        long initialPts; // initial presentation time
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {

                        }

                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                            // if we encounter and end of stream buffer
                            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                                muxer.writeSampleData(track, codec.getOutputBuffer(index), info);
                                // release the muxer
                                muxer.stop();
                                muxer.release();

                                // release the encoder
                                encoder.stop();
                                encoder.release();

                                isRecording = false;

                                return;
                            }
                            if (info.presentationTimeUs != 0) {
                                if (initialPts == 0) {
                                    initialPts = info.presentationTimeUs;
                                    info.presentationTimeUs = 100; // micro seconds
                                } else {
                                    info.presentationTimeUs -= initialPts;
                                }
                            }

                            Log.d(TAG, "onOutputBufferAvailable: pts = " + info.presentationTimeUs );

                            ByteBuffer encodedBuffer = codec.getOutputBuffer(index);

                            // write this buffer data to muxer
                            muxer.writeSampleData(track, encodedBuffer, info);

                            // release this buffer
                            codec.releaseOutputBuffer(index, false);

                        }

                        @Override
                        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

                        }

                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat format) {
                            track = muxer.addTrack(format);
                            muxer.start();

                        }
                    });

                    // configure the encoder
                    DisplayMetrics m = Resources.getSystem().getDisplayMetrics();
                    MediaFormat videoFormat = MediaFormat.createVideoFormat(mimetype,m.widthPixels, m.heightPixels );
                    videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
                    videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
                    videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameFreq);
                    videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);

                    encoder.configure(videoFormat, null ,null , MediaCodec.CONFIGURE_FLAG_ENCODE);
                    Surface surface = encoder.createInputSurface();

                    encoder.start();

                    mediaProj = ((MediaProjectionManager) s.getSystemService(MEDIA_PROJECTION_SERVICE)) .getMediaProjection(-1, s.pData);
                    assert mediaProj != null;

                    virtualDisplay = mediaProj.createVirtualDisplay("virtual display", m.widthPixels, m.heightPixels, m.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null ,null );

                }

                break;

                //stop audio capturing
                case 1: {
                    encoder.signalEndOfInputStream();

                    while (isRecording){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mediaProj.stop();
                    virtualDisplay.release();

                    // stop the service
                    s.stopSelf();

                }
                break;
            }

        }
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case "start": {
                pData = intent.getParcelableExtra("com.example.pData");
                Message m = handler.obtainMessage();
                m.what = 0;
                handler.sendMessage(m);
            } break;
            case "stop": {
                Message m = handler.obtainMessage();
                m.what = 1;
                handler.sendMessage(m);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
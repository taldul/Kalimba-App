package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public class AudioCaptureService extends Service {
    public AudioCaptureService() {
    }

    private class ServiceHandler extends Handler {

        private MediaCodec encoder;
        private MediaMuxer muxer;

        private final String mimetype = MediaFormat.MIMETYPE_AUDIO_MPEG;
        private final String audioUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Recorder Audio.mp3";
        private final int frameRate = 30;
        private final int bitRate = 6 * (int) Math.pow(2, 20) * 8;
        private final int keyFrameFreq = 2;
        private final int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

        public ServiceHandler (Looper looper) {
            super(looper);
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
                }
                break;

                //stop audio capturing
                case 1: {

                }
                break;
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
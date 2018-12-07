package com.android.approjects.exoplayer.extension.flac;

import com.google.android.exoplayer2.extractor.BinarySearchSeeker;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.FlacStreamInfo;

import java.io.IOException;

/**
 * A {@link SeekMap} implementation for FLAC stream using library search.
 *
 * <p>
 *     This seeker performs seeking by using binary search within the stream, until
 *     it finds the frame that contains the target sample.
 * </p>
 */

public class FlacBinarySearchSeeker extends BinarySearchSeeker {

    private final FlacDecoderJni decoderJni;

    protected FlacBinarySearchSeeker(FlacStreamInfo streamInfo,
                                     long firstFramePosition, long inputLength,
                                     FlacDecoderJni decoderJni) {
        super(
                new FlacSeekTimestampConverter(streamInfo),
                new FlacTimestampSeeker(decoderJni),
                streamInfo.durationUs(),
                /* floorTimePosition= */ 0,
                /* ceilingTimePosition= */ streamInfo.totalSamples,
                /* floorBytePosition= */ firstFramePosition,
                /* ceilingBytePosition= */ inputLength,
                /* approxBytesPerFrame= */ streamInfo.getApproxBytesPerFrame(),
                /* minimumSearchRange= */ Math.max(1, streamInfo.minFrameSize));
        this.decoderJni = Assertions.checkNotNull(decoderJni);
    }

    private static final class FlacTimestampSeeker implements TimestampSeeker {

        private final FlacDecoderJni decoderJni;

        private FlacTimestampSeeker(FlacDecoderJni decoderJni) {
            this.decoderJni = decoderJni;
        }

        @Override
        public TimestampSearchResult searchForTimestamp(ExtractorInput input, long targetTimestamp, OutputFrameHolder outputFrameHolder) throws IOException, InterruptedException {
            return null;
        }
    }


    /**
     * A {@link SeekTimestampConverter} implementation that returns teh frame index (sample index) as
     * the timestamp for a stream seek time position.
     */
    private static final class FlacSeekTimestampConverter implements SeekTimestampConverter {

        private final FlacStreamInfo streamInfo;

        public FlacSeekTimestampConverter(FlacStreamInfo streamInfo) {
            this.streamInfo = streamInfo;
        }

        @Override
        public long timeUsToTargetTime(long timeUs) {
            return 0;
        }
    }
}

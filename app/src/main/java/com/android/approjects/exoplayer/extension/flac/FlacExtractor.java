package com.android.approjects.exoplayer.extension.flac;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.PositionHolder;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Facilitates the extraction of data from the FLAC container format.
 */

public class FlacExtractor implements Extractor {

    /**
     * Factory that return one extractor which is a {@link FlacExtractor}
     */
    public static final ExtractorsFactory FACTORY = () -> new Extractor[] {
            new FlacExtractor()
    };

    /**
     * Flags controlling the behavior of the extractor. Possible flag value is {@link
     * #FLAG_DISABLE_ID3_METADATA}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            flag = true,
            value = {FLAG_DISABLE_ID3_METADATA})
    public @interface Flags {}

    /**
     * Flag to disable parsing of ID3 metadata. Can be set to save memory if ID3 metadata is not
     * required.
     */
    public static final int FLAG_DISABLE_ID3_METADATA = 1;

    /**
     * FLAC signature: first 4 is the signature word, second 4 is the sizeof STREAMINFO. 0x22 is the
     * mandatory STREAMINFO.
     */
    private static final byte[] FLAC_SIGNATURE = {'f', 'L', 'a', 'C', 0, 0, 0, 0x22};

    @Override
    public boolean sniff(ExtractorInput input) throws IOException, InterruptedException {
        return false;
    }

    @Override
    public void init(ExtractorOutput output) {

    }

    @Override
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        return 0;
    }

    @Override
    public void seek(long position, long timeUs) {

    }

    @Override
    public void release() {

    }
}

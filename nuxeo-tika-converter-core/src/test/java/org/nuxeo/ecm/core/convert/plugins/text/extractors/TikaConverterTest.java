package org.nuxeo.ecm.core.convert.plugins.text.extractors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.cache.SimpleCachableBlobHolder;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
public class TikaConverterTest {

    @Test
    public void testConvertPlainTextBlob() throws Exception {
        String expectedText = "Software application runs smoothly on all operating systems.";
        File f = FileUtils.getResourceFileFromContext("sample.doc");
        BlobHolder inputHolder = new SimpleCachableBlobHolder(new FileBlob(f));

        TikaConverter converter = new TikaConverter();
        BlobHolder resultHolder = converter.convert(inputHolder, null);

        assertNotNull(resultHolder);
        assertNotNull(resultHolder.getBlob());

        try (InputStream stream = resultHolder.getBlob().getStream()) {
            String resultText = readStream(stream);
            assertEquals(expectedText, resultText.trim());
        }
    }

    private static String readStream(InputStream stream) throws java.io.IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }
}

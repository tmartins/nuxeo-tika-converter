/*
 * (C) Copyright 2026 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */
package org.nuxeo.ecm.core.convert.plugins.text.extractors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.cache.SimpleCachableBlobHolder;
import org.nuxeo.ecm.core.convert.extension.Converter;
import org.nuxeo.ecm.core.convert.extension.ConverterDescriptor;
import org.nuxeo.runtime.api.Framework;
import org.xml.sax.SAXException;

public class TikaConverter implements Converter {

    private static final Logger log = LogManager.getLogger(TikaConverter.class);

    @Override
    public BlobHolder convert(BlobHolder blobHolder, Map<String, Serializable> parameters) throws ConversionException {
        
        log.info("Using TikaConverter");

        Parser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();

        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();

        File f = null;

        try (InputStream input = blobHolder.getBlob().getStream()) {
            parser.parse(input, handler, metadata, context);

            // Get extracted text with Unix end of line characters
            String extractedText = handler.toString().replace("\r\n", "\n");

            byte[] bytes = extractedText.getBytes(StandardCharsets.UTF_8);
            f = Framework.createTempFile("po-msoffice2text", ".txt");
            try (OutputStream fas = new FileOutputStream(f)) {
                fas.write(bytes);
            }

            try (InputStream is = new FileInputStream(f)) {
                Blob blob = Blobs.createBlob(is, "text/plain", "UTF-8");
                return new SimpleCachableBlobHolder(blob);
            }
        } catch (IOException | SAXException | TikaException e) {
            throw new ConversionException("Error during TikaConverter parsing", blobHolder, e);
        } finally {
            if (f != null) {
                try {
                    Files.delete(f.toPath());
                } catch (IOException e) {
                    log.error(e, e);
                }
            }
        }
    }

    @Override
    public void init(ConverterDescriptor descriptor) {
        // nothing to do
    }

}

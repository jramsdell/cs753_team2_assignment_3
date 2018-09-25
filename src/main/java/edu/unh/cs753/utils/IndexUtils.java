package edu.unh.cs753.utils;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains static utility functions used for indexing.
 */
public class IndexUtils {

    /**
     * Function: createParagraphIterator
     * Desc: Using TREC Tools, create an iterator over paragraphs that have been read from a .cbor file.
     * @param cborLoc: Location of the paragraph cbor file to open.
     * @return Iterator over Data.Paragraph
     */
    public static Iterable<Data.Paragraph> createParagraphIterator(String cborLoc) {
        File file = new File(cborLoc);
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return DeserializeData.iterableParagraphs(inputStream);
    }

    /**
     * Function: createPageIterator
     * Desc: Using TREC Tools, create an iterator over pages that have been read from an outlines .cbor file.
     * @param cborLoc: Location of the paragraph cbor file to open.
     * @return Iterator over Data.Page
     */
    public static Iterable<Data.Page> createPageIterator(String cborLoc) {
        File file = new File(cborLoc);
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return DeserializeData.iterableAnnotations(inputStream);
    }

    /**
     * Function: createIndexWriter
     * Desc: Creates an IndexWriter that is responsible for writing new documents to a Lucene index directory.
     *       Note that its OpenMode is set to "create", which means it overwrites existing indexes, so be careful!
     *
     *       Make sure to commit (writer.commit()) every so often, and to close (writer.close()) when done.
     *       The writer is thread-safe, which means you should share it between threads instead of creating one
     *       writer per thread.
     * @param indexLoc: Location of where to create a new Lucene index directory.
     * @return IndexWriter that writes to the user-supplied directory
     */
    public static IndexWriter createIndexWriter(String indexLoc) {
        Path indexPath = Paths.get(indexLoc);
        try {
            FSDirectory indexDir = FSDirectory.open(indexPath);
            IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            return new IndexWriter(indexDir, conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

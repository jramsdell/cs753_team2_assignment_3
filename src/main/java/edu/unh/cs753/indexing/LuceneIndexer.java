package edu.unh.cs753.indexing;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs753.utils.IndexUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

public class LuceneIndexer {
    private final IndexWriter writer;

    public LuceneIndexer(String indexLoc) {
        writer = IndexUtils.createIndexWriter(indexLoc);
    }

    public void doIndex(String cborLoc) throws IOException {
        int counter = 0;
        for (Data.Paragraph p : IndexUtils.createParagraphIterator(cborLoc)) {
            Document doc = new Document();
            doc.add(new StringField("id", p.getParaId(), Field.Store.YES));
            doc.add(new TextField("text", p.getTextOnly(), Field.Store.YES));
            writer.addDocument(doc);
            counter++;
            if (counter % 50 == 0) {
                System.out.println("Commited: " + counter + " paragraphs so far.");
                writer.commit();
            }
        }

        writer.commit();
        writer.close();
    }



}

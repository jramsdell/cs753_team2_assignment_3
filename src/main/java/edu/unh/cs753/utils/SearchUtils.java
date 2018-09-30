package edu.unh.cs753.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class contains static utility functions used for searching a Lucene index and tokenizing queries.
 */
public class SearchUtils {

    /**
     * Function: createIndexSearcher
     * Desc: Creates an IndexSearcher (responsible for querying a Lucene index directory).
     * @param indexLoc: Location of a Lucene index directory.
     * @return IndexSearcher
     */
    public static IndexSearcher createIndexSearcher(String indexLoc) {
        Path indexPath = Paths.get(indexLoc);
        IndexSearcher searcher = null;
        try {
            FSDirectory indexDir = FSDirectory.open(indexPath);
            DirectoryReader reader = DirectoryReader.open(indexDir);
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searcher;
    }

    /**
     * Function: createStandardBooleanQuery
     * Desc: Creates a boolean query (a bunch of terms joined with OR clauses) given a query string.
     *       Note: this is just tokenized using the StandardAnalyzer (so no stemming!).
     *
     * @param queryString: Query string that will be tokenized into query terms.
     * @param termField: The document field that we will be searching against with our query terms.
     * @return
     */
    public static Query createStandardBooleanQuery(String queryString, String termField) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        ArrayList<String> tokens = createTokenList(queryString, new StandardAnalyzer());

        for (String token : tokens) {
            Term term = new Term(termField, token);
            TermQuery termQuery = new TermQuery(term);
            builder.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        return builder.build();
    }

    /**
     * Function: createTokenList
     * Desc: Given a query string, chops it up into tokens and returns an array list of tokens.
     * @param queryString: String to be tokenized
     * @param analyzer: The analyzer responsible for parsing the string.
     * @return A list of tokens (Strings)
     */
    public static ArrayList<String> createTokenList(String queryString, Analyzer analyzer) {
        final ArrayList<String> tokens = new ArrayList<>();

        final StringReader stringReader = new StringReader(queryString);
        try {
            final TokenStream tokenStream = analyzer.tokenStream("text", stringReader);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                tokens.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

}

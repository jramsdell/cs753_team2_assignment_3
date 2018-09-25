package edu.unh.cs753;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import edu.unh.cs753.indexing.LuceneSearcher;
import edu.unh.cs753.indexing.LuceneIndexer;
import edu.unh.cs753.utils.EvaluationUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import kotlin.jvm.functions.Function3;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import utils.KotlinEvaluationUtils;

public class Main {

	// Main class for project
	public static void main(String[] args) throws IOException {
		System.setProperty("file.encoding", "UTF-8");

		String option = args[0];
		String path = args[1];

		if (option.equals("index")) {
			LuceneIndexer indexer  = new LuceneIndexer("paragraphs"); // The directory that will be made
			indexer.doIndex(path);
		}
		else if (option.equals("search")) {
			String cborOutlineLoc = args[2];
			LuceneSearcher searcher = new LuceneSearcher(path, cborOutlineLoc);
			searcher.run();
		} else if (option.equals("custom")) {
			String cborOutlineLoc = args[2];
			LuceneSearcher searcher= new LuceneSearcher(path, cborOutlineLoc);
			searcher.customRun();
		}
		else if (option.equals("evaluate")) {
			String runfileLoc = args[2];
			HashMap<String, HashMap<String, Integer>> relevant = EvaluationUtils.GetRelevantQueries(path);
			HashMap<String, ArrayList<String>> runfileMap = EvaluationUtils.ParseResults(runfileLoc);
			double ndcg20 = KotlinEvaluationUtils.INSTANCE.getNDCG(relevant, runfileMap);
			double map = EvaluationUtils.getMap(relevant, runfileMap);
			double rprec = EvaluationUtils.getPrecisionAtR(relevant, runfileMap);

			System.out.println("Precision @ R: " + rprec);
			System.out.println("MAP: " + map);
			System.out.println("NDCG @ 20: " + ndcg20);

		}

	}

}

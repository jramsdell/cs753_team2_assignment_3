package utils

import edu.unh.cs753.indexing.LuceneSearcher
import edu.unh.cs753.utils.IndexUtils
import edu.unh.cs753.utils.SearchUtils
import org.apache.lucene.search.Query
import org.apache.lucene.search.similarities.BM25Similarity
import java.io.File

private data class DocumentRanking(
        val id: String,
        val score: Double,
        var rank: Int
)

object KotlinSearchUtils {

    public fun runAssignment3Search(indexLoc: String, queryLoc: String) {
//        val searcher = SearchUtils.createIndexSearcher(indexLoc)


        // Write standard page-level and section-level results
        val searcher = LuceneSearcher(indexLoc, queryLoc)
        doPageQueries(searcher) { text: String -> KotlinEvaluationUtils.queryStandard(text, searcher.searcher) }
            .run {  writeResults("page_standard", "standard", this) }
        doSectionQueries(searcher) { text: String -> KotlinEvaluationUtils.queryStandard(text, searcher.searcher) }
            .run {  writeResults("section_standard", "standard", this) }

        // Write lnc.ltn page-level and section-level results
        searcher.searcher.setSimilarity(searcher.createLncSimilarity())
        doPageQueries(searcher) { text: String -> KotlinEvaluationUtils.queryLTN(text, searcher.searcher) }
            .run { writeResults("page_lnc_ltn", "lnc.ltn", this) }
        doSectionQueries(searcher) { text: String -> KotlinEvaluationUtils.queryLTN(text, searcher.searcher) }
            .run { writeResults("section_lnc_ltn", "lnc.ltn", this) }

        // Write bnn.bnn page-level and section-level results
        searcher.searcher.setSimilarity(searcher.createBnnSimilarity())
        doPageQueries(searcher) { text: String -> KotlinEvaluationUtils.queryBNN(text, searcher.searcher) }
            .run { writeResults("page_bnn_bnn", "bnn.bnn", this) }
        doSectionQueries(searcher) { text: String -> KotlinEvaluationUtils.queryBNN(text, searcher.searcher) }
            .run { writeResults("section_bnn_bnn", "bnn.bnn", this) }

        // Write anc.apc page-level and section-level results
        searcher.searcher.setSimilarity(searcher.createAncSimilarity())
        doPageQueries(searcher) { text: String -> KotlinEvaluationUtils.queryAPC(text, searcher.searcher) }
            .run { writeResults("page_anc_apc", "anc.apc", this) }
        doSectionQueries(searcher) { text: String -> KotlinEvaluationUtils.queryAPC(text, searcher.searcher) }
            .run { writeResults("section_anc_apc", "anc.apc", this) }

    }

    private fun doPageQueries(searcher: LuceneSearcher, queryCreator: (String) -> Query) =
        searcher.pages
            .map { page ->
                val name = page.pageName
                val results = searcher.doSearch(queryCreator(name))
                getRankings(page.pageId, results)
            }

    private fun doSectionQueries(searcher: LuceneSearcher, queryCreator: (String) -> Query) =
            searcher.pages
                .flatMap { page ->
                    val sections = page.flatSectionPaths()
                        .map { page.pageName + " " + it.map { it.heading }.joinToString(" ") to
                                page.pageId + "/" + it.map { it.headingId }.joinToString("/") } +
                            (page.pageName to page.pageId)


                    val runResults = sections.map { (queryName, queryId) ->
                        val results = searcher.doSearch(queryCreator(queryName))
                        getRankings(queryId, results)
                    }
                    runResults }


    private fun getRankings(id: String, results: List<LuceneSearcher.idScore>) =
            results
                .groupBy { it.i }
                .map { it.key to it.value.maxBy { it.s }!!.s.toDouble() }
                .sortedByDescending { (_, score) -> score }
                .mapIndexed { index, idScore ->
                    DocumentRanking(id = idScore.first, score = idScore.second, rank = index + 1) }
                .let { rankings -> id to rankings }


    private fun writeResults(outName: String, methodName: String, results: List<Pair<String, List<DocumentRanking>>>) {
        File("results/")
            .run { if (!exists()) mkdir() }

        val out = File("results/$outName.run")
            .bufferedWriter()

        results.forEach { (query, rankings) ->
            rankings
                .map { (id, score, rank) -> "$query Q0 $id $rank $score $methodName" }
                .joinToString("\n")
                .let { rankingsString -> out.write(rankingsString + "\n") }
        }

        out.close()
    }

    fun getPages(cborLoc: String) =
            IndexUtils.createPageIterator(cborLoc)
                .toList()

}
package main;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.StemmerOverrideFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import files.FolderFiles;

public class Lucene {
	public static void main(String [] args) throws IOException {



		StandardAnalyzer analyzer = new StandardAnalyzer();
		FolderFiles folderFiles = new FolderFiles("../Text 2/Vendas online/", analyzer);
		folderFiles.readFiles();

		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		IndexWriter w;
		try {
			w = new IndexWriter(index, config);
			folderFiles.addFiles(w, 200);
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner sc = new Scanner(System.in);
		System.out.println("Digite o termo de pesquisa");
		String querystr = sc.nextLine();
		while (!querystr.equalsIgnoreCase("exit")) {
			querystr = FolderFiles.preparacaoDados(analyzer, "query", querystr, true, true);
			querystr = querystr.replaceAll(" ", " AND ");

			System.out.println(querystr);
			try {
				HashMap<String, Float> weights = new HashMap<>();
				weights.put("text", 1F);
				weights.put("title", 3F);
				Query q = new MultiFieldQueryParser(new String[] {"text", "title"}, analyzer, weights).parse(querystr);


				int hitsPerPage = 10;
				IndexReader reader = DirectoryReader.open(index);
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs docs = searcher.search(q, hitsPerPage);
				ScoreDoc[] hits = docs.scoreDocs;

				System.out.println("Found " + hits.length + " hits.");
				String path = "";
				for(int i=0;i<hits.length;++i) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					System.out.println((i + 1) + ". " + d.get("title") + "\t\t" + d.get("text").substring(0, 100));
					path = d.get("path");
				}
				if (path.length() > 1) {
					List<String> file = Files.readAllLines(Paths.get(path));
					String text = String.join(" ",file);
					System.out.println("\n" + text + "\n");
				}


			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Digite o termo de pesquisa");
			querystr = sc.nextLine();
		}
		sc.close();
	}
}

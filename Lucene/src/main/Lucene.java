package main;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

		boolean stopWords = false;
		boolean stemming = false;

		int consulta = 1;
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Digite 0 para a consulta (Corrida Eleitoral) ou 1 para (Análise de Vendas)");

		if (sc.next().equalsIgnoreCase("1")) {
			consulta = 2;
		}
		
		System.out.println("Digite 1 para usar stopwords ou 0 para não usar");
		
		if (sc.next().equalsIgnoreCase("1")) {
			stopWords = true;
		}
		
		System.out.println("Digite 1 para usar stemming ou 0 para não usar");
		if (sc.next().equalsIgnoreCase("1")) {
			stemming = true;
		}
	
		System.out.println("Consulta: " + consulta);
		System.out.println("StopWords: " + stopWords);
		System.out.println("Stemming: " + stemming);
		
		consulta(stopWords, stemming, consulta);

		sc.close();

	}
	
	private static void consulta(boolean stopWords, boolean stemming, int consulta) throws IOException {
		
		System.out.println("Processando documentos...");
		String consulta1FilesPath = "./Relevantes Corrida.txt";
		String consulta2FilesPath = "./Relevantes Vendas.txt";

		List<String> positivosConsulta1 = Arrays.asList(String.join("", Files.readAllLines(
				Paths.get(new File(consulta1FilesPath).getAbsolutePath()))).split(";"));
		List<String> positivosConsulta2 = Arrays.asList(String.join("", Files.readAllLines(
				Paths.get(new File(consulta2FilesPath).getAbsolutePath()))).split(";"));

		String consulta1 = "Corrida Eleitoral";
		String consulta2 = "Analise de Vendas";

		StandardAnalyzer analyzer = new StandardAnalyzer();
		FolderFiles folderFiles = new FolderFiles("../Text 2/Vendas online/", 
				"../Text 2/Ciencia de dados/", "../Text 2/Eleicoes 2018/", 
				analyzer, stopWords, stemming);
		folderFiles.readFiles();

		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		IndexWriter w;
		try {
			w = new IndexWriter(index, config);
			folderFiles.addFiles(w, 500);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String querystr = consulta1;
		if (consulta == 2) {
			querystr = consulta2;
		}
		querystr = FolderFiles.preparacaoDados(analyzer, "query", querystr, stopWords, stemming);
		querystr = querystr.replaceAll(" ", " AND ");

		System.out.println("Query: " + querystr);
		try {
			Query q = new MultiFieldQueryParser(new String[] {"text", "title"}, analyzer).parse(querystr);


			int hitsPerPage = 500;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;

			System.out.println("Encontrou " + hits.length + " documentos.");
			List<String> positivosConsultaFiles = positivosConsulta1;
			if (consulta == 2) {
				positivosConsultaFiles = positivosConsulta2;
			}
			
			int relevantes = 0;
			for(int i=0;i<hits.length;++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				String path = d.get("path");
				
				for (String file: positivosConsultaFiles) {
					if (path.equalsIgnoreCase(file)) {
						relevantes += 1;
						break;
					}
				}
			}
			System.out.println("Documentos Relevantes: " + relevantes);
			
			float precisao = (float) relevantes/hits.length;
			float cobertura = (float) relevantes/positivosConsultaFiles.size();
			
			float fMeasure = (float) 2*(precisao*cobertura)/(precisao + cobertura);
			System.out.println();
			System.out.println("Precisão: " + precisao);
			System.out.println("Cobertura: " + cobertura);
			System.out.println("F-Measure: " + fMeasure);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
}

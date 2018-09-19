package files;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class FolderFiles {
	private String folder1, folder2, folder3;
	private ArrayList<File> files;
	private Analyzer analyzer;
	private boolean stopWords;
	private boolean stemming;
	
	public FolderFiles(String folder1, String folder2, String folder3, 
			Analyzer analyzer, boolean stopwords, boolean stemming) {
		this.folder1 = folder1;
		this.folder2 = folder2;
		this.folder3 = folder3;
		this.files = new ArrayList<>();
		this.analyzer = analyzer;
		this.stopWords = stopwords;
		this.stemming = stemming;
	}

	public void readFiles() {
		ArrayList<File> listOfFiles = new ArrayList<>(Arrays.asList((new File(this.folder1)).listFiles()));
		listOfFiles.addAll(Arrays.asList((new File(this.folder2).listFiles())));
		listOfFiles.addAll(Arrays.asList((new File(this.folder3).listFiles())));

//		listOfFiles.sort(new Comparator<File>() {
//			@Override
//	        public int compare(File f1, File f2) {
//	            return f1.getName().compareTo(f2.getName());
//	        }
//		});
		for (File f : listOfFiles) {
//			System.out.println(f.getAbsolutePath());
			if (f.isFile() && f.getName().endsWith(".txt")) {
				this.files.add(f);
			}
		}
	}

	public void addFiles(IndexWriter w) throws IOException {
		addFiles(w, 2);
	}

	public void addFiles(IndexWriter w, int n) throws IOException {
//		System.out.println("tam array" + this.files.size());
		n = Math.min(files.size(),n);
		for (int i = 0;i<n;i++) {
			File f = files.get(i);
			List<String> file = Files.readAllLines(Paths.get(files.get(i).getAbsolutePath()));
			String title = file.get(0);
			file.remove(0);
			String text = String.join(" ",file);
//			String title1 = preparacaoDados("title", title, false, false);
//			String title2 = preparacaoDados("title", title, true, false);
//			String title3 = preparacaoDados("title", title, false, true);
			String title4 = preparacaoDados(analyzer, "title", title, this.stopWords, this.stemming);
			
//			String text1 = preparacaoDados("text", text, false, false);
//			String text2 = preparacaoDados("text", text, true, false);
//			String text3 = preparacaoDados("text", text, false, true);
			String text4 = preparacaoDados(analyzer, "text", text, this.stopWords, this.stemming);
			
			List<String> filePath = Arrays.asList(f.getAbsolutePath().split("/../Text 2/"));
//			System.out.println();
			addDoc(w, title4, text4, "./" + filePath.get(1));
		}
	}

	private void addDoc(IndexWriter w, String title, String text, String path) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new TextField("text", text, Field.Store.YES));
		doc.add(new TextField("path", path, Field.Store.YES));
		w.addDocument(doc);
	}

	public static String preparacaoDados(Analyzer analyzer, String field, String input, boolean stop, boolean stem) throws IOException {
		TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(input));
		if (stop) {
			tokenStream = new StopFilter(tokenStream, BrazilianAnalyzer.getDefaultStopSet());
		}
		if (stem) {
			tokenStream = new PorterStemFilter(tokenStream);
		}
		CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);

		String text = "";
		try {
			tokenStream.reset();

			tokenStream.incrementToken();
			text = termAtt.toString();
			while (tokenStream.incrementToken()) {
				text += " " + termAtt.toString();
			}
			tokenStream.end();
		} finally {
			tokenStream.close();
		}
		//		System.out.println("\n");
		return text;
	}
}

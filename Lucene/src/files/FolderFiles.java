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
	private String folder;
	private ArrayList<File> files;
	private Analyzer analyzer;
	public FolderFiles(String folder, Analyzer analyzer) {
		this.folder = folder;
		this.files = new ArrayList<>();
		this.analyzer = analyzer;
	}

	public void readFiles() {
		File folder = new File(this.folder);
		ArrayList<File> listOfFiles = new ArrayList<>(Arrays.asList(folder.listFiles()));

//		listOfFiles.sort(new Comparator<File>() {
//			@Override
//	        public int compare(File f1, File f2) {
//	            return f1.getName().compareTo(f2.getName());
//	        }
//		});
		for (File f : listOfFiles) {
			System.out.println(f.getName());
			if (f.isFile() && f.getName().endsWith(".txt")) {
				this.files.add(f);
			}
		}
	}

	public void addFiles(IndexWriter w) throws IOException {
		addFiles(w, 2);
	}

	public void addFiles(IndexWriter w, int n) throws IOException {
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
			String title4 = preparacaoDados(analyzer, "title", title, true, true);
			
//			String text1 = preparacaoDados("text", text, false, false);
//			String text2 = preparacaoDados("text", text, true, false);
//			String text3 = preparacaoDados("text", text, false, true);
			String text4 = preparacaoDados(analyzer, "text", text, true, true);
			
			String filePath = f.getAbsolutePath();
			
			addDoc(w, title4, text4, filePath);
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

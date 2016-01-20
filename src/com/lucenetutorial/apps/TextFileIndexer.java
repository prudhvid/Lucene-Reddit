package com.lucenetutorial.apps;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.codecs.compressing.*;
import org.apache.lucene.codecs.compressing.CompressingStoredFieldsFormat;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat;
import org.apache.lucene.codecs.lucene54.Lucene54Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.BinaryDocValuesField;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;



/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class TextFileIndexer {
  private static StandardAnalyzer analyzer = new StandardAnalyzer();

  private IndexWriter writer;
  private ArrayList<File> queue = new ArrayList<File>();
  

  public static void main(String[] args) throws IOException {
    System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");

    String indexLocation = null;
    BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in));
    String s = br.readLine();

    TextFileIndexer indexer = null;
    try {
      indexLocation = s;
      indexer = new TextFileIndexer(indexLocation);
    } catch (Exception ex) {
      System.out.println("Cannot create index..." + ex.getMessage());
      System.exit(-1);
    }

    //===================================================
    //read input from user until he enters q for quit
    //===================================================
//    while (!s.equalsIgnoreCase("q")) {
//      try {
//        System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
//        System.out.println("[Acceptable file types: .xml, .html, .html, .txt]");
//        s = br.readLine();
//        if (s.equalsIgnoreCase("q")) {
//          break;
//        }
//
//        //try to add file into the index
//        indexer.indexFileOrDirectory(s);
//      } catch (Exception e) {
//        System.out.println("Error indexing " + s + " : " + e.getMessage());
//      }
//    }

    //===================================================
    //after adding, we always have to call the
    //closeIndex, otherwise the index is not created    
    //===================================================
    indexer.closeIndex();

    IndexReader reader;
      reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation).toPath()));
      
    IndexSearcher searcher = new IndexSearcher(reader);
    
    
    s = "";
    while (!s.equalsIgnoreCase("q")) {
      try {
        System.out.println("Enter the search query (q=quit):");
        s = br.readLine();
        if (s.equalsIgnoreCase("q")) {
          break;
        }
        Query q = new QueryParser( "body", analyzer).parse(s);
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(5);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
//          String path=CompressionTools.decompressString(d.getBinaryValue("path"));
//          String fname=CompressionTools.decompressString(d.getBinaryValue("filename"));
//          System.out.println((i + 1) + ". " + path + " score=" + hits[i].score+" name="+fname);
            System.out.println(d.get("id")+" "+d.get("body"));
        }
        
        try{
            Long id=Long.valueOf(s);
            Query q2 = NumericRangeQuery.newLongRange("id",  id, id, true, true);
            collector = TopScoreDocCollector.create(5);
            searcher.search(q2, collector);
            
            hits = collector.topDocs().scoreDocs;

            // 4. display results
            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
              int docId = hits[i].doc;
              Document d = searcher.doc(docId);
    //          String path=CompressionTools.decompressString(d.getBinaryValue("path"));
    //          String fname=CompressionTools.decompressString(d.getBinaryValue("filename"));
    //          System.out.println((i + 1) + ". " + path + " score=" + hits[i].score+" name="+fname);
                System.out.println(d.get("id")+" "+d.get("body"));
            }
            
        }
        catch(NumberFormatException ex){
            
        }
      } catch (Exception e) {
        System.out.println("Error searching " + s + " : " + e.getMessage());
      }
    }

  }

  /**
   * Constructor
   * @param indexDir the name of the folder in which the index should be created
   * @throws java.io.IOException when exception creating index.
   */
  TextFileIndexer(String indexDir) throws IOException {
    // the boolean true parameter means to create a new index everytime, 
    // potentially overwriting any existing files there.
    FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());


    IndexWriterConfig config = new IndexWriterConfig( analyzer);
    Lucene50StoredFieldsFormat lucene50StoredFieldsFormat = new Lucene50StoredFieldsFormat(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION);
    
    config.setCodec(new Lucene54Codec(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION));
//    config.setCodec(new CompressingCodec());
    
//    config.setCodec(new SimpleTextCodec());
    
    writer = new IndexWriter(dir, config);
  }

  /**
   * Indexes a file or directory
   * @param fileName the name of a text file or a folder we wish to add to the index
   * @throws java.io.IOException when exception
   */
  public void indexFileOrDirectory(String fileName) throws IOException {
    //===================================================
    //gets the list of files in a folder (if user has submitted
    //the name of a folder) or gets a single file name (is user
    //has submitted only the file name) 
    //===================================================
    addFiles(new File(fileName));
    
    int originalNumDocs = writer.numDocs();
    for (File f : queue) {
      FileReader fr = null;
      try {
        Document doc = new Document();

        //===================================================
        // add contents of file
        //===================================================
        fr = new FileReader(f);
        
//        CompressingStoredFieldsFormat format=new CompressingStoredFieldsFormat("MEMORY_CHUNK",CompressionMode.HIGH_COMPRESSION,1000000,
//            10000,100000);
        
//        CompressingStoredFieldsFormat formats=new CompressingStoredFieldsFormat();
        
        File file = new File(f.getAbsolutePath());
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        BytesRef x=new BytesRef( CompressionTools.compressString("Hello world"));
        String str = new String(data, "UTF-8");
        
//        CompressionTools.compressString(, java.util.zip.Deflater.BEST_COMPRESSION);
//        doc.add(new BinaryDocValuesField("contents", new BytesRef (CompressionTools.compress(data)),Field.Store.NO));
        
        doc.add(new TextField("contents",fr));
//        doc.add(new );
        
        doc.add(new StoredField("path", CompressionTools.compressString(f.getPath())));
        doc.add(new StoredField("path2", CompressionTools.compressString(f.getPath())));
        doc.add(new StoredField("path3", CompressionTools.compressString(f.getPath())));
        doc.add(new StoredField("path4", CompressionTools.compressString(f.getPath())));
        doc.add(new StoredField("path5", CompressionTools.compressString(f.getPath())));
        doc.add(new StoredField("path6", CompressionTools.compressString(f.getPath())));
        doc.add(new StoredField("path7", CompressionTools.compressString(f.getPath())));
        
        doc.add(new StoredField("filename", CompressionTools.compressString(f.getName())));
        doc.add(new StoredField("filename2", CompressionTools.compressString(f.getName())));
        doc.add(new StoredField("filename3", CompressionTools.compressString(f.getName())));
        doc.add(new StoredField("filename4", CompressionTools.compressString(f.getName())));
        doc.add(new StoredField("filename5", CompressionTools.compressString(f.getName())));
        
//        doc.add(new BinaryDocValuesField("path", new BytesRef())));
//        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
//        doc.add(new StringField("path2", f.getPath(), Field.Store.YES));
//        doc.add(new StringField("path3", f.getPath(), Field.Store.YES));
//        doc.add(new StringField("path4", f.getPath(), Field.Store.YES));
//        doc.add(new StringField("path5", f.getPath(), Field.Store.YES));
//        doc.add(new StringField("path6", f.getPath(), Field.Store.YES));
//        doc.add(new StringField("path7", f.getPath(), Field.Store.YES));
////        doc.add(new BinaryDocValuesField("filename", new BytesRef(CompressionTools.compressString(f.getName()))));
//        doc.add(new StringField("filename", f.getName(), Field.Store.YES));
//        doc.add(new StringField("filename2", f.getName(), Field.Store.YES));
//        doc.add(new StringField("filename3", f.getName(), Field.Store.YES));
//        doc.add(new StringField("filename4", f.getName(), Field.Store.YES));
//        doc.add(new StringField("filename5", f.getName(), Field.Store.YES));
        

        writer.addDocument(doc);
        System.out.println("Added: " + f);
      } catch (Exception e) {
        System.out.println("Could not add: " + f);
      } finally {
        fr.close();
      }
    }
    
    int newNumDocs = writer.numDocs();
    System.out.println("");
    System.out.println("************************");
    System.out.println((newNumDocs - originalNumDocs) + " documents added.");
    System.out.println("************************");

    queue.clear();
  }

  private void addFiles(File file) {

    if (!file.exists()) {
      System.out.println(file + " does not exist.");
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        addFiles(f);
      }
    } else {
      String filename = file.getName().toLowerCase();
      //===================================================
      // Only index text files
      //===================================================
      if (filename.endsWith(".htm") || filename.endsWith(".html") || 
              filename.endsWith(".xml") || filename.endsWith(".txt") || filename.endsWith(".cpp") 
              || filename.endsWith(".c") || filename.endsWith(".py") || filename.endsWith(".java")) {
        queue.add(file);
      } else {
        System.out.println("Skipped " + filename);
      }
    }
  }

  /**
   * Close the index.
   * @throws java.io.IOException when exception closing
   */
  public void closeIndex() throws IOException {
    writer.close();
  }
}
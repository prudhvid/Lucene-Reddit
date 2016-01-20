package com.lucenetutorial.apps;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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

import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat;
import org.apache.lucene.codecs.lucene54.Lucene54Codec;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.util.BytesRef;



/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class Searcher {
  private static StandardAnalyzer analyzer = new StandardAnalyzer();

  private IndexWriter writer;
  private ArrayList<File> queue = new ArrayList<>();
  static int NDOCS=5;

  public static void main(String[] args) throws IOException {
    
    
    if(args.length<1){
        System.err.println("Index path required!");
        System.exit(-1);
    }
    if(args.length>=2){
        NDOCS=Integer.parseInt(args[1]);
    }
        
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    String indexLocation = null;
    String s = args[0];

    Searcher indexer = null;
    try {
      indexLocation = s;
      indexer = new Searcher(indexLocation);
    } catch (Exception ex) {
      System.out.println("Cannot create index..." + ex.getMessage());
      System.exit(-1);
    }


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
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(NDOCS);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
            System.out.println(d.get("id")+" "+d.get("body"));
        }
        
        try{
            Long id=Long.valueOf(s);
            Query q2 = NumericRangeQuery.newLongRange("id",  id, id, true, true);
            collector = TopScoreDocCollector.create(NDOCS);
            searcher.search(q2, collector);
            
            hits = collector.topDocs().scoreDocs;

            // 4. display results
            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
              int docId = hits[i].doc;
              Document d = searcher.doc(docId);
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
  Searcher(String indexDir) throws IOException {
    // the boolean true parameter means to create a new index everytime, 
    // potentially overwriting any existing files there.
    FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());


    IndexWriterConfig config = new IndexWriterConfig( analyzer);
    Lucene50StoredFieldsFormat lucene50StoredFieldsFormat = new Lucene50StoredFieldsFormat(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION);
    
    config.setCodec(new Lucene54Codec(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION));

    
    writer = new IndexWriter(dir, config);
  }

  /**
   * Close the index.
   * @throws java.io.IOException when exception closing
   */
  public void closeIndex() throws IOException {
    writer.close();
  }
}
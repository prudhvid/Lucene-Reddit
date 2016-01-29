package com.lucenetutorial.apps;


import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.compressing.CompressingStoredFieldsFormat;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat;
import org.apache.lucene.codecs.lucene54.Lucene54Codec;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author prudhvi
 */
public class CompressingCodec extends FilterCodec{

 /**
 * @see
CompressingStoredFieldsFormat#CompressingStoredFieldsFormat(CompressionMode, int,
CompressingStoredFieldsIndex)
 */
 public CompressingCodec() {

    super("CompressingCodec", new Lucene54Codec(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION));
 }


 @Override
 public StoredFieldsFormat storedFieldsFormat() {
    
    return new MyStoredFieldFormat();
 }
    
}


class MyStoredFieldFormat extends CompressingStoredFieldsFormat {

    public MyStoredFieldFormat(){
        super("MyStoredFieldFormat", CompressionMode.HIGH_COMPRESSION, 1<<28, 1<<28, 1<<27);
// super(null, CompressionMode.FAST, chunkSize, maxDocsPerChunk, blockSize)
//For a chunk size of chunkSize bytes, this StoredFieldsFormat does not support documents larger than (2^31 - chunkSize) bytes.
        
    }
}
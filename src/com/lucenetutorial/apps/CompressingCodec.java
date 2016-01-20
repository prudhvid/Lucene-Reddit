package com.lucenetutorial.apps;


import java.util.Random;
import javax.print.attribute.standard.Compression;
import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.compressing.CompressingStoredFieldsFormat;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.lucene50.Lucene50CompoundFormat;
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
    

 private final CompressingStoredFieldsFormat storedFieldsFormat;

 /**
 * @see
CompressingStoredFieldsFormat#CompressingStoredFieldsFormat(CompressionMode, int,
CompressingStoredFieldsIndex)
 */
 public CompressingCodec() {

    super("Compressing", new Lucene54Codec(Lucene50StoredFieldsFormat.Mode.BEST_COMPRESSION));
    
//    this.storedFieldsFormat = new CompressingStoredFieldsFormat(compressionMode,chunkSize, storedFieldsIndexFormat);
    this.storedFieldsFormat=new CompressingStoredFieldsFormat
        (Compression.DEFLATE.getName(), CompressionMode.HIGH_COMPRESSION, 100000, 1000, 100000);
     }

// public CompressingCodec() {
//
//this(CompressionMode.FAST, 1 << 14, CompressingStoredFieldsIndex.MEMORY_CHUNK);
// }

 @Override
 public StoredFieldsFormat storedFieldsFormat() {
    
    return storedFieldsFormat;
 }
    
}

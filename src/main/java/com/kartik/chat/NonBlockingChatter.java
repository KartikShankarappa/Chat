package com.kartik.chat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingChatter implements Chatter {
	
	private static final int BUFFER_SIZE = 1024; 
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final SocketChannel chatServerChannel;
	private final Pipe.SourceChannel userInput;
	private final ByteBuffer readBuffer;
	private final ByteBuffer writeBuffer;

    public NonBlockingChatter(SocketChannel chatServerChannel,
                              Pipe.SourceChannel userInput) {
    	
    	this.chatServerChannel = chatServerChannel;
    	this.userInput = userInput;
    	readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    	writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    	
    }

    @Override public void run() {
        // TODO
    	try {
			Selector selector = Selector.open();
			
			chatServerChannel.configureBlocking(false);
			userInput.configureBlocking(false);
			
			chatServerChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			userInput.register(selector, SelectionKey.OP_READ);
			
			while(!Thread.currentThread().isInterrupted()) {
				
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				
				while(keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					Channel channel = key.channel();
					
					if (key.isReadable()) {
						nowReading(key, channel);
					}else if (key.isWritable()) {
						nowWriting(key, channel);
					}
					
					keyIterator.remove();
				}
			}
			
		} catch (IOException e) {
			System.out.printf("Some problem has occured: %s%n", e.getMessage());
		} 
    	
    }
    
    
    private void nowReading(SelectionKey key, Channel channel) throws IOException {
    	
    	if(channel == userInput) {
    		int bytesRead = userInput.read(readBuffer);
    		
    		if(bytesRead != -1) {
    			readBuffer.flip();
    			while(readBuffer.hasRemaining()) {
    				writeBuffer.put(readBuffer.get());
    			}
    		}
    		
    		readBuffer.clear();
    		
    	}else if (channel == chatServerChannel) {
    		
    		int bytesRead = chatServerChannel.read(readBuffer);
    		
    		if(bytesRead == -1) {
    			return;
    		}else {
    			readBuffer.flip();
    			CharsetDecoder decoder = UTF8.newDecoder();
    			CharBuffer tempBuffer = decoder.decode(readBuffer);
    			System.out.printf("%s", tempBuffer.toString());
    			
    			readBuffer.clear();
    		}
    		
    	}
    	
    }
    

    private void nowWriting(SelectionKey Key, Channel channel) throws IOException {
    	
    	if (channel == userInput) {
    	}
    	
    	if(channel == chatServerChannel) {
    		writeBuffer.flip();
    		chatServerChannel.write(writeBuffer);
    		writeBuffer.clear();
    	}
    	
    }

}

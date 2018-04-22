package com.kartik.chat;

import java.io.*;
import java.util.Random;

public class BlockingChatter implements Chatter {
	
	/**
	 * A static method to count the number of lines in a given file
	 * @param filename
	 * @return	count	the number of lines in a given file
	 * @throws IOException
	 */
    private static int countNumberOfLines(String filename) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
        try {
            int lineCount = 0;
            int readChars = 0;
            byte[] tempArray = new byte[1024];
            boolean endsWithoutNewLine = false;
            while ((readChars = inputStream.read(tempArray)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (tempArray[i] == '\n')
                        ++lineCount;
                }
                endsWithoutNewLine = (tempArray[readChars - 1] != '\n');
            }
            if(endsWithoutNewLine) {
                ++lineCount;
            } 
            return lineCount;
        } finally {
            inputStream.close();
        }
    }
    

	public final InputStream chatServerInput;
	public final OutputStream chatServerOutput;
	public final InputStream userInput;

    public BlockingChatter(InputStream chatServerInput, OutputStream chatServerOutput, InputStream userInput) {
        // TODO
    	this.chatServerInput = chatServerInput;
    	this.chatServerOutput = chatServerOutput;
    	this.userInput = userInput;
    }

    @Override public void run() {
        // TODO
    	
		//This method is called to process the user input
    	processUserInput();

    	//This method is called to process the server input
    	processServerInput();
		
    }
    
    /**
     * A method to process the user input
     */
    private void processUserInput() {
    	
    	Thread userInputReaderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				
				while (!Thread.currentThread().isInterrupted()) {
					
					String input;
					
					try {
						
						InputStreamReader inputStreamReader = new InputStreamReader(userInput);
						BufferedReader userInputReader = new BufferedReader(inputStreamReader);
						input = userInputReader.readLine();
						
						chatServerOutput.write(input.getBytes());
						
					} catch (IOException e) {
						System.out.printf("Some error has occured %s%n", e.getMessage());
					} 
				}
			}
		});
    	
    	userInputReaderThread.start();
    }
    
    /**
     * A method to process the server input
     */
    private void processServerInput() {
    	
    	Thread chatServerInputReaderThread = new Thread (new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
									
					String input;
					
					try {
						
						int noOfLinesMobyDick = countNumberOfLines("src/main/resources/Moby Dick.txt/");
						
						InputStreamReader inputStreamReader = new InputStreamReader(chatServerInput);
						BufferedReader chatServerInputReader = new BufferedReader(inputStreamReader);
						
						input = chatServerInputReader.readLine();
						
						String randomLine = "";
						
						String[] splittedWords = input.split(" ");
						
						if (splittedWords[1].equals("java")) {
							
							Random random = new Random();
							int randomLineNumber = random.nextInt(noOfLinesMobyDick);
							
							FileReader fileReader = new FileReader("src/main/resources/Moby Dick.txt");
							BufferedReader fileInputReader = new BufferedReader(fileReader);
							
							randomLine = fileInputReader.readLine();
							for (int i = 0; i <= randomLineNumber; i++) {
								randomLine = fileInputReader.readLine();
							}
							
							fileInputReader.close();

						}
						
						chatServerOutput.write(randomLine.getBytes());
						System.out.printf("%s%n", input);
						
					} catch (IOException e) {
						System.out.printf("Some error has occured %s%n", e.getMessage());
					} 	
				}
			}
		});
		
		chatServerInputReaderThread.start();
    }

}

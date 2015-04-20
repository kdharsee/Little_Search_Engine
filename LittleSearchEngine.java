package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException{
		HashMap<String,Occurrence> kwo = new HashMap<String,Occurrence>();
		File file = new File(docFile);
		Scanner sc = new Scanner(file);
		
		while(sc.hasNext())
		{
			String keyWord = getKeyWord(sc.next());
			
			if(keyWord != null)
			{
				if(kwo.containsKey(keyWord))
				{
					kwo.get(keyWord).frequency++;
				}
				else
				{
					Occurrence occur = new Occurrence(docFile, 1);
					kwo.put(keyWord, occur);
				}
			}
		}
		return kwo;
	}
	
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		Iterator<String> itr = kws.keySet().iterator();
		while (itr.hasNext()){
			String wordlabel = itr.next().toString();
			if(keywordsIndex.containsKey(wordlabel)){
				Occurrence temp = new Occurrence(kws.get(wordlabel).document, kws.get(wordlabel).frequency);
				keywordsIndex.get(wordlabel).add(temp);
				insertLastOccurrence(keywordsIndex.get(wordlabel));
			}
			else 
			{
				Occurrence temp = new Occurrence(kws.get(wordlabel).document, kws.get(wordlabel).frequency);
				ArrayList<Occurrence> occur = new ArrayList<Occurrence>();
				occur.add(temp);
				keywordsIndex.put(wordlabel, occur);
			}
		}
	
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		if (word.equals(null)||word.equals("")) return null;

		word = word.trim().toLowerCase();
		
		if (word.charAt(0) == '(' || word.charAt(0) == '{') word = word.substring(1);
		if (word.charAt(word.length()-1) == ')' || word.charAt(word.length()-1) == '}') word = word.substring(0, word.length()-1);
		
		while(!word.equals("") &&!Character.isLetter(word.charAt(word.length()-1)))
		{
			if(word.charAt(word.length()-1) == '.' ||word.charAt(word.length()-1) == ',' ||word.charAt(word.length()-1) == '?' ||word.charAt(word.length()-1) == ':' ||word.charAt(word.length()-1) == ';' ||word.charAt(word.length()-1) == '!')
				word = word.substring(0, word.length()-1);
			else return null;	
		}
		if (word.equals("") || word.equals(null)) return null;
		
		if (!Character.isLetter(word.charAt(0))) return null;
		
		for(int i = 0; i < word.length() - 1; i ++)
			if(!Character.isLetter(word.charAt(i))) return null;
		
		if (noiseWords.containsValue(word)) return null;
		
		return word.toLowerCase();
	}
		
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		
		if(occs==null)
		{
			return null;
		}
		ArrayList<Integer> midpts = new ArrayList<Integer>();
		midpts=binarySearch(occs);
		int index = midpts.get(midpts.size()-1);
		midpts.remove(midpts.size()-1);
		if(occs.get(index).frequency>occs.get(occs.size()-1).frequency)
		{
			occs.add(index+1, occs.get(occs.size()-1));
		}
		else 
		{
			occs.add(index, occs.get(occs.size()-1));
		}
		occs.remove(occs.size()-1);
		if (occs.size() == 1) return null;
		return midpts;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		kw1 = kw1.trim().toLowerCase();
		kw2 = kw2.trim().toLowerCase();
		ArrayList<Occurrence> kw1occs = keywordsIndex.get(kw1);
		ArrayList<Occurrence> kw2occs = keywordsIndex.get(kw2);
		bubbleSort(kw1occs);
		bubbleSort(kw2occs);
		int count = 0;
		int i1 = 0;
		int i2 = 0;
		ArrayList<String> result = new ArrayList<String>(5);
		if(!keywordsIndex.containsKey(kw1)){
			if(!keywordsIndex.containsKey(kw2)){
				return null;
			}
			while(count < 5){
				if (kw2occs.size()-i2-1 < 0) return result;
				result.add(kw2occs.get(kw2occs.size()-i2-1).document);
				count++;
				i2++;
			}
			return result;
		}
		else if(!keywordsIndex.containsKey(kw2)){
			while(count < 5){
				if (kw1occs.size()-i1-1 < 0) return result;
				result.add(kw1occs.get(kw1occs.size()-i1-1).document);
				count++;
				i1++;
			}
			return result;
		}
		while (count < 5){
			if (kw1occs.size()-i1-1 < 0 && kw2occs.size()-i2-1 < 0){
				return result;
			}
			else if(kw1occs.size()-i1-1 < 0){
				if (!result.contains(kw2occs.get(kw2occs.size()-i2-1).document)){
					count++;
					result.add(kw2occs.get(kw2occs.size()-i2-1).document);
				}
				i2++;
			}
			else if(kw2occs.size()-i2-1 < 0){
				if (!result.contains(kw1occs.get(kw1occs.size()-i1-1).document)){
					count++;
					result.add(kw1occs.get(kw1occs.size()-i1-1).document);
				}
				i1++;
			}
			else{
				
				if (kw1occs.get(kw1occs.size()-i1-1).frequency > kw2occs.get(kw2occs.size()-i2-1).frequency){
					if (!result.contains(kw1occs.get(kw1occs.size()-i1-1).document)){
						count++;
						result.add(kw1occs.get(kw1occs.size()-i1-1).document);
					}
					i1++;
				}
				else if (kw1occs.get(kw1occs.size()-i1-1).frequency < kw2occs.get(kw2occs.size()-i2-1).frequency){
					if (!result.contains(kw2occs.get(kw2occs.size()-i2-1).document)){
						count++;
						result.add(kw2occs.get(kw2occs.size()-i2-1).document);
					}
					i2++;
				}
				else{ 
					if (!result.contains(kw1occs.get(kw1occs.size()-i1-1).document)){
						count++;
						result.add(kw1occs.get(kw1occs.size()-i1-1).document);
					}
					i1++;
				}
			}
			
		}
		
		if (result.size() == 0) return null;
		
		return result;
	}
	private static void bubbleSort(ArrayList<Occurrence> occs) {
		if (occs == null) return;
		boolean swapped = true;
		int j = 0;
		Occurrence tmp;
		while (swapped) {
		      swapped = false;
		      j++;
		      for (int i = 0; i < occs.size()- j; i++) {                                       
		    	  if (occs.get(i).frequency > occs.get(i + 1).frequency) {                          
	                  tmp = occs.get(i);
	                  occs.remove(i);
	                  occs.add(i+1, tmp);
	                  swapped = true;
	             }
	        }                
		}
	}
	private static ArrayList<Integer> binarySearch(ArrayList<Occurrence> occs){
		int lo=0;
		int hi=occs.size()-2;
		ArrayList<Integer> midpts = new ArrayList<Integer>();
		while(lo<hi)
		{
			int mid=(lo+hi)/2;
			midpts.add(mid);
			
			if((occs.get(occs.size()-1)).frequency == occs.get(mid).frequency){
				midpts.add(mid+1);
				return midpts;
			}
			if((occs.get(occs.size()-1)).frequency < occs.get(mid).frequency){
				
				lo=mid+1;
			}
			else
			{
				hi=mid-1;
			}
		}
		midpts.add(lo);
		return midpts;
		
		
	}
}

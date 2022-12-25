package edu.estu;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class App {
    public static void main(String[] args) throws IOException {
        String url = args[0];
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("body");

        ArrayList<String> text = new ArrayList<>();
        for (Element element : elements) {
            text.add(element.text());
        }

       StringBuilder sb = new StringBuilder();
       for (String s : text) {
           sb.append(s);
           sb.append("\t");
       }

        String allText = text.toString();
        ArrayList<String> allWordsAsArray = new ArrayList<>();

        for (String sentence : sentenceDetection(allText)) {
            allWordsAsArray.addAll(tokenizerApp(sentence));
        }

        String[] allWords = arrayListToArray(allWordsAsArray);

        for (String person : Objects.requireNonNull(findNames(allWords))) {
            System.out.println(person);
        }
    }

    public static ArrayList<String> sentenceDetection(String text) {
        ArrayList<String> list = new ArrayList<>();
        try (InputStream modelIn = new FileInputStream("src/main/resources/en-sent.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(model);
            String[] sentences = sentenceDetectorME.sentDetect(text);
            Collections.addAll(list, sentences);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return list;
    }

    public static ArrayList<String> tokenizerApp(String text) {
        ArrayList<String> list = new ArrayList<>();
        try (InputStream modelIn = new FileInputStream("src/main/resources/en-token.bin")) {
            TokenizerModel model = new TokenizerModel(modelIn);
            Tokenizer tokenizer = new TokenizerME(model);
            String[] tokens = tokenizer.tokenize(text);
            Collections.addAll(list, tokens);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return list;
    }

    public static String[] findNames(String[] tokens) {
        try (InputStream modelIn = new FileInputStream("src/main/resources/en-ner-person.bin")) {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            NameFinderME nameFinder = new NameFinderME(model);
            Span[] nameSpans = nameFinder.find(tokens);
            String[] names = Span.spansToStrings(nameSpans, tokens);

            if (names.length > 0) {
                return names;
            }
            nameFinder.clearAdaptiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] arrayListToArray(ArrayList<String> arrayList) {
        String[] array = new String[arrayList.size()];

        for (int i = 0; i < arrayList.size(); i++) {
            array[i] = arrayList.get(i);
        }

        return array;

    }


}


import java.util.*;
import java.io.*;

public class Main {

    private Map<String, List<DocumentReference>> invertedIndex;

    private List<Document> documents;

    public Main() {
        invertedIndex = new HashMap<>();
        documents = new ArrayList<>();
    }

    public static class Document {
        private int id;
        private String content;
        private Map<String, Integer> termFrequency;

        public Document(int id, String content) {
            this.id = id;
            this.content = content;
            this.termFrequency = new HashMap<>();

            String[] terms = tokenize(content);
            for (String term : terms) {
                termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
            }
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public Map<String, Integer> getTermFrequency() {
            return termFrequency;
        }

        public int getTermCount(String term) {
            return termFrequency.getOrDefault(term, 0);
        }

        public int getTotalTerms() {
            return termFrequency.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    public static class DocumentReference {
        private Document document;
        private double score;

        public DocumentReference(Document document) {
            this.document = document;
            this.score = 0.0;
        }

        public Document getDocument() {
            return document;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    private static String[] tokenize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .trim()
                .split("\s+");
    }

    public void addDocument(String content) {
        // Create a new document with the next available ID
        int docId = documents.size();
        Document doc = new Document(docId, content);
        documents.add(doc);

        // Update the inverted index with terms from this document
        String[] terms = tokenize(content);
        Set<String> uniqueTerms = new HashSet<>(Arrays.asList(terms));

        for (String term : uniqueTerms) {
            if (!invertedIndex.containsKey(term)) {
                invertedIndex.put(term, new ArrayList<>());
            }
            invertedIndex.get(term).add(new DocumentReference(doc));
        }
    }

    private double calculateTfIdf(String term, Document doc) {
        double tf = (double) doc.getTermCount(term) / doc.getTotalTerms();
        int docFrequency = invertedIndex.getOrDefault(term, Collections.emptyList()).size();
        double idf = Math.log((double) documents.size() / (docFrequency + 1));

        return tf * idf;
    }

    public List<Document> search(String query) {
        String[] queryTerms = tokenize(query);

        // If the query has multiple terms we'll use a simple approach
        // by combining the TF-IDF scores for each term
        Map<Integer, Double> documentScores = new HashMap<>();

        for (String term : queryTerms) {
            if (invertedIndex.containsKey(term)) {
                List<DocumentReference> references = invertedIndex.get(term);

                for (DocumentReference ref : references) {
                    Document doc = ref.getDocument();
                    double score = calculateTfIdf(term, doc);
                    documentScores.put(doc.getId(),
                            documentScores.getOrDefault(doc.getId(), 0.0) + score);
                }
            }
        }
        List<Document> results = new ArrayList<>();
        documentScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .forEach(entry -> results.add(documents.get(entry.getKey())));

        return results;
    }

    public static void main(String[] args) {
        Main engine = new Main();

        // Add sample documents
        engine.addDocument("The brown fox jumped over the brown dog");
        engine.addDocument("The lazy brown dog sat in the corner");
        engine.addDocument("The red fox bit the lazy dog");

        // Test searches
        System.out.println("Search results for 'brown':");
        List<Document> results1 = engine.search("brown");
        for (Document doc : results1) {
            System.out.println("Document " + doc.getId() + ": " + doc.getContent());
        }

        System.out.println("\nSearch results for 'fox':");
        List<Document> results2 = engine.search("fox");
        for (Document doc : results2) {
            System.out.println("Document " + doc.getId() + ": " + doc.getContent());
        }

        System.out.println("\nSearch results for 'lazy dog':");
        List<Document> results3 = engine.search("lazy dog");
        for (Document doc : results3) {
            System.out.println("Document " + doc.getId() + ": " + doc.getContent());
        }
    }
}
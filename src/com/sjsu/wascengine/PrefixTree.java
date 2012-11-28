package com.sjsu.wascengine;
/**
 * A PrefixTree to store keywords. Keywords have their corresponding weights,
 * rubric, and how many times they have been found stored in the leaf nodes
 * 
 * @author Michael Riha
 */
public class PrefixTree
{
    private Node root;
    
    /** Constructs an empty PrefixTree */
    public PrefixTree() { root = new Node(); }
    
    /**
     * A node in the tree which has up to 26 children and a weighted value
     * If the weight is 0, then the node is not a leaf on the tree
     * Also stores the associated rubric and how many times a word has been used
     */
    public class Node
    {
        private int rubric;
        private int weight;
        private int occurrences;
        private Node[] children;

        private Node()
        { 
            rubric = 0;
            weight = 0;
            occurrences = 0;
            children = new Node[26];
        }

        public int getRubric() { return rubric; }
        public int getWeight() { return weight; }
        public int getOccurrences() { return occurrences; }
        public Node[] getChildren() { return children; }
    }
    
    /**
     * Adds a word to the PrefixTree with corresponding weight and rubric values 
     * Does not allow duplicates or words whose prefix is already in the tree.
     * @param word the word to add
     * @param weight the weight of the word
     * @param rubric which rubric the word is associated with
     * @return true if the word was added, false if it was already in the tree
     */
    public boolean add(String word, int weight, int rubric)
    {
        word = word.toLowerCase();
        Node cur = root;
        int next_idx;
        
        for (int i = 0; i < word.length(); ++i)
        {            
            if (word.charAt(i) == '-' && ++i == word.length()) // skip hyphens 
                break;
            // add the node. if is already weighted then return false
            next_idx = (int) (word.charAt(i) - 'a');
            if (cur.children[next_idx] == null)
                if (cur.weight == 0)
                    cur.children[next_idx] = new Node();
                else 
                    return false;
            cur = cur.children[next_idx]; // descend the tree
        }
        // reached a leaf so set the weight
        if (cur.weight == 0) 
        {
            cur.rubric = rubric;
            cur.weight = weight;
            return true;
        }
        else return false; // already in the tree           
    }
    
    /**
     * Find a word in the tree iteratively, if it exists, including wildcards
     * E.g. if "critic" is in the tree, "critically" will map to that
     * @param word the word to look for
     * @return the leaf node associated with this word, null if not found
     */
    public Node find(String word)
    {
        word = word.toLowerCase();
        Node cur = root;
        Node next;
        
        for (int i = 0; i < word.length(); ++i)
        {
            if (cur.weight > 0) // "wildcard" found early
            {
                ++cur.occurrences;
                return cur;
            }            
            if (word.charAt(i) == '-' && ++i == word.length())
                break;
            next = cur.children[(int) word.charAt(i) - 'a'];            
            if (next == null)// word not in tree
                return null;
            cur = next;
        }
        if (cur.weight > 0)
        {
            ++cur.occurrences;                
            return cur;
        }
        else return null;
    }    
    
    /**
     * Same as find but does not increment the number of occurrences of word
     * @param word the word to look for
     * @return the leaf node associated with this word, null if not found
     */
    public Node findNoIncrement(String word)
    {
        word = word.toLowerCase();
        Node cur = root;
        Node next;
        
        for (int i = 0; i < word.length(); ++i)
        {
            if (cur.weight > 0) // "wildcard" found early
                return cur;            
            if (word.charAt(i) == '-' && ++i == word.length()) // skip hyphens 
                break;
            next = cur.children[(int) word.charAt(i) - 'a'];            
            if (next == null) // word not in tree
                return null;            
            cur = next; // descend the tree
        }
        if (cur.weight > 0) 
            return cur;
        else return null;
    }
    
    /** Sets the number of occurrences of each word in tree to 0 recursively  */
    public void reset() { resetHelper(root); }
    
    private void resetHelper(Node node)
    {
        if (node.occurrences != 0)
            node.occurrences = 0;
        else 
            for (int i = 0; i < 26; ++i)
                if (node.children[i] != null)
                    resetHelper(node.children[i]);
                else break;
    }
}

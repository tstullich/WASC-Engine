package com.sjsu.wascengine;
/**
 * A PrefixTree to store keywords. Keywords have their corresponding weights
 * stored in the leaves of the tree.
 * 
 * @author Michael Riha
 */
public class PrefixTree
{
    private Node root;
    
    /**
     * A node in the tree which has up to 26 children and a weighted value
     * If the weight is 0, then the node is not a leaf on the tree
     * Also stores the associated rubric and how many times a word has been
     * found
     */
    public class Node
    {
        private int rubric;
        private int weight;
        private int occurrences;
        private Node[] children;

        public Node()
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
     * Constructs an empty PrefixTree
     */
    public PrefixTree()
    {
        root = new Node();
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
            next_idx = (int) (word.charAt(i) - 'a');
            
            // add node that wasn't there unless this node is already weighted
            if (cur.children[next_idx] == null)
                if (cur.weight == 0)
                    cur.children[next_idx] = new Node();
                else return false;
            
            cur = cur.children[next_idx]; // descend the tree
        }
        
        if (cur.weight == 0) // reached a leaf so set the weight
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
     * @return the leaf node associated with this word
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
            next = cur.children[(int) word.charAt(i) - 'a'];
            
            if (next == null) // word not in tree
                return null;
            
            cur = next; // descend the tree
        }
        ++cur.occurrences;                
        return cur;
    }    
    
    /** Sets the number of occurrences of each word to 0 recursively  */
    public void reset()
    {
        resetHelper(root);
    }
    
    /** 
     * recursive helper for reset 
     * @param node the Node to try resetting
     */
    private void resetHelper(Node node)
    {
        if (node != null)
            if (node.occurrences != 0)
                node.occurrences = 0;
            else 
                for (int i = 0; i < 25; ++i)
                    resetHelper(node.children[i]);
    }
}

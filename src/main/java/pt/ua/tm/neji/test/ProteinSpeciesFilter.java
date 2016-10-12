
/*
 * Copyright (c) 2016 BMD Software and University of Aveiro.
 *
 * Neji is a flexible and powerful platform for biomedical information extraction from text.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it.
 * However, you may not use it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package pt.ua.tm.neji.test;

import com.google.common.collect.Multimap;
import monq.jfa.DfaRun;
import pt.ua.tm.neji.core.annotation.Annotation;
import pt.ua.tm.neji.core.annotation.AnnotationImpl;
import pt.ua.tm.neji.core.annotation.Identifier;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.tree.Tree;
import pt.ua.tm.neji.tree.TreeNode;

import java.util.*;

public class ProteinSpeciesFilter extends BaseLoader {

    private String proteinGroup, speciesGroup;
    private Multimap<String, String> proteinTospecies;

    public ProteinSpeciesFilter(final Multimap<String, String> proteinToSpecies,
                                final String proteinGroup, final String speciesGroup) throws NejiException {
        super(DfaRun.UNMATCHED_COPY);
        super.addActionToGoofedElement(text_action, "s");
        this.proteinTospecies = proteinToSpecies;
        this.proteinGroup = proteinGroup;
        this.speciesGroup = speciesGroup;

//        try {
//            Nfa nfa = new Nfa(Nfa.NOTHING);
//            nfa.or(Xml.GoofedElement("s"), eof);
//            setNFA(nfa, DfaRun.UNMATCHED_COPY);
//        } catch (ReSyntaxException ex) {
//            throw new NejiException(ex);
//        }
    }

//    public ProteinSpeciesFilter(final Corpus corpus, final Multimap<String, String> proteinToSpecies,
//                                final String proteinGroup, final String speciesGroup) throws NejiException {
//        this(proteinToSpecies, proteinGroup, speciesGroup);
//        getPipeline().setCorpus(corpus);
//    }

    private DefaultAction text_action = new DefaultAction() {
        @Override
        public void execute(StringBuffer yytext, int start) {
            Corpus corpus = getPipeline().getCorpus();
            Set<String> mentionedSpeciesIDs = getMentionedSpeciesIDs(corpus);

            for (Sentence sentence : corpus) {

                List<TreeNode<Annotation>> nodes = sentence.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
                for (TreeNode<Annotation> node : nodes) {
                    AnnotationImpl annotation = (AnnotationImpl)node.getData();

                    boolean containsProtein = false;
                    List<Identifier> identifiersToRemove = new ArrayList<>();

                    for (Identifier identifier : annotation.getIDs()) {
                        if (identifier.getGroup().equalsIgnoreCase(proteinGroup)) {
                            containsProtein = true;
                            Collection<String> proteinSpecies = proteinTospecies.get(identifier.getID());

                            boolean contains = false;
                            for (String ps : proteinSpecies) {
                                if (mentionedSpeciesIDs.contains(ps)) {
                                    contains = true;
                                }
                            }

                            if (!contains) {
                                identifiersToRemove.add(identifier);
                            }

//                            if (!mentionedSpeciesIDs.contains(proteinSpecies)) {
//                                identifiersToRemove.add(identifier);
//                            }
                        }
                    }

                    // Remove identifiers from annotation
                    if (containsProtein) {
                        annotation.getIDs().removeAll(identifiersToRemove);
                        boolean d = true;
                    }

                    // Check if it is empty
//                    if (annotation.getIDs().isEmpty()) {
//                        // Remove annotation from parent
//                        TreeNode<DefaultAnnotation> parent = node.getParent();
//                        if (parent == null){
//                            node.removeChildren();
//                        } else {
//                            parent.removeChildAt(parent.getChildren().indexOf(node));
//                        }
//
//                    }
                }
            }
        }
    };

    private Set<String> getMentionedSpeciesIDs(Corpus corpus) {
        Set<String> identifiers = new HashSet<>();
        for (Sentence sentence : corpus) {
            List<Annotation> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
            for (Annotation annotation : annotations) {
                AnnotationImpl a = (AnnotationImpl)annotation;
                for (Identifier identifier : a.getIDs()) {
                    if (identifier.getGroup().equalsIgnoreCase(speciesGroup)) {


                        String id = identifier.getID();
                        // Temporary hack
//                        if (id.equals("10114")) {
//                            id = "10116";
//                        }
//                        if (id.equals("10088")) {
//                            id = "10090";
//                        }

                        identifiers.add(id);
                    }
                }
            }
        }
        return identifiers;
    }
}

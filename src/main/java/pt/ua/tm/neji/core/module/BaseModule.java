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

package pt.ua.tm.neji.core.module;


import monq.jfa.*;
import monq.jfa.actions.Replace;
import pt.ua.tm.neji.core.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.Sentence;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.pipeline.DefaultPipeline;

/**
 * Abstract class that integrates base functionality of a {@link Module}.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public abstract class BaseModule implements Module {

    private Pipeline pipeline;
    private Nfa nfa;
    private Dfa dfa;
    private FaAction eof_action;
    private final DfaRun.FailedMatchBehaviour failedBehaviour;


    /**
     * Main constructor that starts a new NFA and acknowledges what should be the failed match behavior for the NFA.
     * @param failedBehaviour the failed match behavior that will occur in case NFA compilation fails
     */
    protected BaseModule(final DfaRun.FailedMatchBehaviour failedBehaviour) {
        setPipeline(new DefaultPipeline());
        this.nfa = new Nfa(Nfa.NOTHING);
        this.dfa = null;
        this.eof_action = null;
        this.failedBehaviour = failedBehaviour;
    }

//    /**
//     * Constructor that starts a new NFA and acknowledges what should be the failed match behavior for the NFA,
//     * as well as set a new Corpus for this module.
//     * @param corpus the corpus that will be used by this module
//     * @param failedBehaviour the failed match behavior that will occur in case NFA compilation fails
//     */
//    protected BaseModule(final Corpus corpus, final DfaRun.FailedMatchBehaviour failedBehaviour) {
//        this(failedBehaviour);
//        this.getPipeline().setCorpus(corpus);
//    }

    /**
     * {@inheritDoc}
     */
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * {@inheritDoc}
     */
    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * {@inheritDoc}
     *
     * In addition, this method will perform an initial check to the modules implemented
     * BaseAction methods, according to the defined parameters in the module's constructor.
     * If a specific module is defined to have a BaseAction for a certain tag and if
     * the respective method was not overridden, an exception is thrown.
     */
    @Override
    public final void compile() throws NejiException {
        try {
            if(dfa==null){
                if(eof_action!=null){
                    this.dfa = nfa.compile(failedBehaviour, eof_action);
                    pipeline.storeModuleData(this, dfa);
                } else {
                    this.dfa = nfa.compile(failedBehaviour);
                    pipeline.storeModuleData(this, dfa);
                }
            }
        } catch (CompileDfaException ex) {
            throw new NejiException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Dfa getDFA() throws NejiException {
        if (dfa == null) {
            throw new RuntimeException("DFA module was not compiled! Must use the 'compile()' method first!");
        }
        return dfa;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Nfa getNFA()throws NejiException {
        if (dfa == null) {
            throw new RuntimeException("DFA module was not compiled! Must use the 'compile()' method first!");
        }
        return dfa.toNfa();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DfaRun getRun()throws NejiException {
        if (dfa == null) {
            throw new RuntimeException("DFA module was not compiled! Must use the 'compile()' method first!");
        }
        return dfa.createRun();
    }






    protected void addReplaceAction(String oldElement, String newElement) throws NejiException {
        try{
            nfa.or(oldElement, new Replace(newElement));
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }

    protected void addActionToGoofedElement(Action actionClass, String... elements) throws NejiException {
        if(!(actionClass instanceof DefaultAction))
            throw new NejiException("The module "+this.getClass().getSimpleName()+" called the " +
                    "method 'addActionToGoofedElement(Action, String...)' with an invalid Action class.\n" +
                    "Must only use DefaultAction or SentenceIteratorDefaultAction.");

        try{
            for(String element : elements)
                nfa.or(Xml.GoofedElement(element), actionClass.getAction());
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }

    protected void addActionToRegex(Action actionClass, String regex) throws NejiException {
        if(!(actionClass instanceof DefaultAction))
            throw new NejiException("The module "+this.getClass().getSimpleName()+" called the " +
                    "method 'addActionToRegex(Action, String)' with an invalid Action class.\n" +
                    "Must only use DefaultAction or SentenceIteratorDefaultAction.");

        addActionToTag(actionClass, regex);
    }

    protected void addActionToTag(Action actionClass, String... tags) throws NejiException {
        if(actionClass instanceof EofAction)
            throw new NejiException("The module "+this.getClass().getSimpleName()+" called the " +
                    "method 'addActionToTag(Action, String...)' with an invalid Action class.\n" +
                    "Must only use DefaultAction, SentenceIteratorDefaultAction, StartAction, " +
                    "SentenceIteratorStartAction, EndAction or SentenceIteratorEndAction.");

        try{
            for(String tag : tags)
                nfa.or(tag, actionClass.getAction());
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }

    protected void addActionToXMLTag(Action actionClass, String... tags) throws NejiException {
        if(actionClass instanceof EofAction)
            throw new NejiException("The module "+this.getClass().getSimpleName()+" called the " +
                    "method 'addActionToXMLTag(Action, String...)' with an invalid Action class.\n" +
                    "Must only use DefaultAction, SentenceIteratorDefaultAction, StartAction, " +
                    "SentenceIteratorStartAction, EndAction or SentenceIteratorEndAction.");
        try{
            for(String tag : tags) {
                if(actionClass instanceof StartAction){
                    nfa.or(Xml.STag(tag), actionClass.getAction());
                } else if(actionClass instanceof EndAction){
                    nfa.or(Xml.ETag(tag), actionClass.getAction());
                } else if(actionClass instanceof DefaultAction){
                    nfa.or(Xml.EmptyElemTag(tag), actionClass.getAction());
                }
            }
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }

    protected void setEofAction(EofAction actionClass) throws NejiException {
        this.eof_action = actionClass.getAction();
    }

    private abstract class BaseAction implements Action {

        private FaAction action;

        private BaseAction(){}

        public abstract void execute(StringBuffer text, int startIndex);

        public final FaAction getAction(){
            return action;
        }
    }

    protected abstract class DefaultAction extends BaseAction {

        protected DefaultAction() {
            super.action = new AbstractFaAction() {
                @Override
                public void invoke(StringBuffer yytext, int start, DfaRun runner) {
                    execute(yytext, start);
                }
            };
        }

        @Override
        public abstract void execute(StringBuffer yytext, int start);

    }

    protected abstract class StartAction extends BaseAction {

        protected StartAction() {
            super.action = new AbstractFaAction() {
                @Override
                public void invoke(StringBuffer yytext, int start, DfaRun runner) {
                    runner.collect = true;
                    execute(yytext, start);
                }
            };
        }

        @Override
        public abstract void execute(StringBuffer yytext, int start);

    }

    protected abstract class EndAction extends BaseAction {

        protected EndAction() {
            super.action = new AbstractFaAction() {
                @Override
                public void invoke(StringBuffer yytext, int start, DfaRun runner) {
                    execute(yytext, start);
                    runner.collect = false;
                }
            };
        }

        @Override
        public abstract void execute(StringBuffer yytext, int start);

    }

    protected abstract class SentenceIteratorDefaultAction extends DefaultAction {

        private int sentenceCounter;

        protected SentenceIteratorDefaultAction() {
            super();
            this.sentenceCounter = 0;
        }

        @Override
        public final void execute(StringBuffer yytext, int start) {
            Sentence nextSentence = getPipeline().getCorpus().getSentence(sentenceCounter);
            execute(yytext, start, nextSentence);
            sentenceCounter++;
        }

        public abstract void execute(StringBuffer yytext, int start, Sentence nextSentence);
    }

    protected abstract class SentenceIteratorStartAction extends StartAction {

        private int sentenceCounter;

        protected SentenceIteratorStartAction() {
            super();
            this.sentenceCounter = 0;
        }

        @Override
        public final void execute(StringBuffer yytext, int start) {
            Sentence nextSentence = getPipeline().getCorpus().getSentence(sentenceCounter);
            execute(yytext, start, nextSentence);
            sentenceCounter++;
        }

        public abstract void execute(StringBuffer yytext, int start, Sentence nextSentence);
    }
    
    protected abstract class SentenceIteratorEndAction extends EndAction {

        private int sentenceCounter;

        protected SentenceIteratorEndAction() {
            super();
            this.sentenceCounter = 0;
        }

        @Override
        public final void execute(StringBuffer yytext, int start) {
            Sentence nextSentence = getPipeline().getCorpus().getSentence(sentenceCounter);
            execute(yytext, start, nextSentence);
            sentenceCounter++;
        }

        public abstract void execute(StringBuffer yytext, int start, Sentence nextSentence);
    }

    protected abstract class EofAction extends EndAction {

        protected EofAction(){
            super();
        }

    }

}

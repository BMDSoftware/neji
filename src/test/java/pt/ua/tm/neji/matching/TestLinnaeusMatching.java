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

package pt.ua.tm.neji.matching;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.dictionary.VariantMatcherLoader;
import pt.ua.tm.neji.exception.NejiException;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.util.List;

/**
 * Testing class for Linnaeus VariantDictionary loading and matching.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TestLinnaeusMatching extends TestCase {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TestLinnaeusMatching.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestLinnaeusMatching(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestLinnaeusMatching.class);
    }

    public void testCase1() throws NejiException {
        //Test Case 1: one entry of a word in a matcher

        Matcher matcher = new VariantMatcherLoader(true)
                .load("UMLS:C0001338:T047:DISO\tblindness|blindness, day|hemeralopias|hemeralopia|day blindness")
                .getMatcher();
        String sentence = "And they struck with blindness the men who were at the entrance of the house.";
        List<Mention> mentionList = matcher.match(sentence);
        Mention mention = mentionList.get(0);

        assertEquals(5, matcher.size());
        assertEquals("[UMLS:C0001338:T047:DISO\tnull\t21\t30\tblindness\t]",
                mentionList.toString());
        assertEquals(1, mentionList.size());

        assertEquals("blindness", sentence.substring(mention.getStart(), mention.getEnd()));
        assertEquals("blindness", mention.getText());
        assertEquals("UMLS:C0001338:T047:DISO", mention.getIds()[0]);
    }

    public void testCase2() throws NejiException {
        //Test Case 2: two entries of a word in the same matcher

        Matcher matcher = new VariantMatcherLoader(true)
                .load("UMLS:C0001338:T047:DISO\tblindness|blindness, day|hemeralopias|hemeralopia|day blindness")
                .load("UMLS:C0001339:T047:DISO\tblindness|blindness, night|nyctalopia|night blindness")
                .getMatcher();
        String sentence = "And they struck with blindness the men who were at the entrance of the house.";
        List<Mention> mentionList = matcher.match(sentence);
        Mention mention = mentionList.get(0);

        assertEquals(8, matcher.size());
        assertEquals("[UMLS:C0001338:T047:DISO|UMLS:C0001339:T047:DISO\tnull\t21\t30\tblindness\t]",
                mentionList.toString());
        assertEquals(1, mentionList.size());

        assertEquals("blindness", sentence.substring(mention.getStart(), mention.getEnd()));
        assertEquals("blindness", mention.getText());
        assertEquals("UMLS:C0001338:T047:DISO", mention.getIds()[0]);
        assertEquals("UMLS:C0001339:T047:DISO", mention.getIds()[1]);
    }

    public void testCase3() throws NejiException {
        //Test Case 3: two different words in the same matcher

        Matcher matcher = new VariantMatcherLoader(true)
                .load("UMLS:C0001209:T023:ANAT\tacromion|acromions")
                .load("UMLS:C0155017:T047:DISO\ttritan defect|tritanomaly|blue color blindness|tritanopia")
                .getMatcher();
        String sentence = "Any man with tritan defect can still have acromions";
        List<Mention> mentionList = matcher.match(sentence);
        Mention mention1 = mentionList.get(0);
        Mention mention2 = mentionList.get(1);

        assertEquals(6, matcher.size());
        assertEquals("[UMLS:C0155017:T047:DISO\tnull\t13\t26\ttritan defect\t, UMLS:C0001209:T023:ANAT\tnull\t42\t51\tacromions\t]",
                mentionList.toString());
        assertEquals(2, mentionList.size());

        assertEquals(13, mention1.getStart());
        assertEquals(26, mention1.getEnd());
        assertEquals("tritan defect", sentence.substring(mention1.getStart(), mention1.getEnd()));
        assertEquals("tritan defect", mention1.getText());
        assertEquals("UMLS:C0155017:T047:DISO", mention1.getIds()[0]);

        assertEquals(42, mention2.getStart());
        assertEquals(51, mention2.getEnd());
        assertEquals("acromions", sentence.substring(mention2.getStart(), mention2.getEnd()));
        assertEquals("acromions", mention2.getText());
        assertEquals("UMLS:C0001209:T023:ANAT", mention2.getIds()[0]);
    }

    public void testCase4() throws NejiException {
        //Test Case 4: two words in different matchers

        Matcher matcher1 = new VariantMatcherLoader(true)
                .load("UMLS:C0001209:T023:ANAT\tacromion|acromions")
                .getMatcher();
        Matcher matcher2 = new VariantMatcherLoader(true)
                .load("UMLS:C0155017:T047:DISO\ttritan defect|tritanomaly|blue color blindness|tritanopia")
                .getMatcher();
        String sentence = "Any man with tritan defect can still have acromions";
        Mention mention1 = matcher1.match(sentence).get(0);
        Mention mention2 = matcher2.match(sentence).get(0);

        assertEquals(2, matcher1.size());
        assertEquals("[UMLS:C0001209:T023:ANAT\tnull\t42\t51\tacromions\t]",
                matcher1.match(sentence).toString());
        assertEquals(1, matcher1.match(sentence).size());
        assertEquals(4, matcher2.size());
        assertEquals("[UMLS:C0155017:T047:DISO\tnull\t13\t26\ttritan defect\t]",
                matcher2.match(sentence).toString());
        assertEquals(1, matcher2.match(sentence).size());

        assertEquals(42, mention1.getStart());
        assertEquals(51, mention1.getEnd());
        assertEquals("acromions", sentence.substring(mention1.getStart(), mention1.getEnd()));
        assertEquals("acromions", mention1.getText());
        assertEquals("UMLS:C0001209:T023:ANAT", mention1.getIds()[0]);

        assertEquals(13, mention2.getStart());
        assertEquals(26, mention2.getEnd());
        assertEquals("tritan defect", sentence.substring(mention2.getStart(), mention2.getEnd()));
        assertEquals("tritan defect", mention2.getText());
        assertEquals("UMLS:C0155017:T047:DISO", mention2.getIds()[0]);
    }

    public void testCase5() throws NejiException {
        //Test Case 5: two different words from different matchers with two entries in each matcher

        Matcher matcher1 = new VariantMatcherLoader(true)
                .load("UMLS:C0001338:T047:DISO\tblindness|blindness, day|hemeralopias|hemeralopia|day blindness")
                .load("UMLS:C0001339:T047:DISO\tblindness|blindness, night|nyctalopia|night blindness")
                .getMatcher();
        Matcher matcher2 = new VariantMatcherLoader(true)
                .load("UMLS:C0003055:T023:ANAT\ttritan defect|tritanomaly")
                .load("UMLS:C0000905:T023:ANAT\ttritan defect|blue color blindness")
                .getMatcher();
        String sentence = "Any man with tritan defect also has blindness";
        Mention mention1 = matcher1.match(sentence).get(0);
        Mention mention2 = matcher2.match(sentence).get(0);

        assertEquals(8, matcher1.size());
        assertEquals("[UMLS:C0001338:T047:DISO|UMLS:C0001339:T047:DISO\tnull\t36\t45\tblindness\t]",
                matcher1.match(sentence).toString());
        assertEquals(1, matcher1.match(sentence).size());
        assertEquals("[UMLS:C0000905:T023:ANAT|UMLS:C0003055:T023:ANAT\tnull\t13\t26\ttritan defect\t]",
                matcher2.match(sentence).toString());
        assertEquals(1, matcher2.match(sentence).size());

        assertEquals("blindness", sentence.substring(mention1.getStart(), mention1.getEnd()));
        assertEquals("tritan defect", sentence.substring(mention2.getStart(), mention2.getEnd()));
    }

    public void testCase6() throws NejiException {
        //Test Case 6: two nested words in a matcher

        Matcher matcher = new VariantMatcherLoader(true)
                .load("UMLS:C0155017:T047:DISO\tblindness|tritan blindness|tritanomaly|blue color blindness")
                .getMatcher();
        String sentence = "A man with tritan blindness must be sad";
        List<Mention> mentionList = matcher.match(sentence);
        Mention mention1 = mentionList.get(0);
        Mention mention2 = mentionList.get(1);

        assertEquals(4, matcher.size());
        assertEquals("[UMLS:C0155017:T047:DISO\tnull\t11\t27\ttritan blindness\t, UMLS:C0155017:T047:DISO\tnull\t18\t27\tblindness\t]",
                mentionList.toString());
        assertEquals(2, mentionList.size());

        assertEquals("tritan blindness", sentence.substring(mention1.getStart(), mention1.getEnd()));
        assertEquals("tritan blindness", mention1.getText());
        assertEquals("UMLS:C0155017:T047:DISO", mention1.getIds()[0]);

        assertEquals("blindness", sentence.substring(mention2.getStart(), mention2.getEnd()));
        assertEquals("blindness", mention2.getText());
        assertEquals("UMLS:C0155017:T047:DISO", mention2.getIds()[0]);

        assertTrue(mention1.overlaps(mention2));
    }

    public void testCase7() throws NejiException {
        //Test Case 7: two intersected words in a matcher

        Matcher matcher = new VariantMatcherLoader(true)
                .load("UMLS:C0155017:T047:DISO\ttritan defect|blue color blindness")
                .load("UMLS:C0155018:T048:DISO\tdefect blindness|tritanomaly")
                .getMatcher();
        String sentence = "A man with tritan defect blindness must be sadder";
        List<Mention> mentionList = matcher.match(sentence);
        Mention mention1 = mentionList.get(0);
        Mention mention2 = mentionList.get(1);

        assertTrue(mention1.getEnd()>=mention2.getStart());
        assertTrue(mention1.overlaps(mention2));

        assertEquals(4, matcher.size());
        assertEquals("[UMLS:C0155017:T047:DISO\tnull\t11\t24\ttritan defect\t, UMLS:C0155018:T048:DISO\tnull\t18\t34\tdefect blindness\t]",
                mentionList.toString());
        assertEquals(2, mentionList.size());

        assertEquals("tritan defect", sentence.substring(mention1.getStart(), mention1.getEnd()));
        assertEquals("tritan defect", mention1.getText());
        assertEquals(11, mention1.getStart());
        assertEquals(24, mention1.getEnd());
        assertEquals("UMLS:C0155017:T047:DISO", mention1.getIds()[0]);

        assertEquals("defect blindness", sentence.substring(mention2.getStart(), mention2.getEnd()));
        assertEquals("defect blindness", mention2.getText());
        assertEquals(18, mention2.getStart());
        assertEquals(34, mention2.getEnd());
        assertEquals("UMLS:C0155018:T048:DISO", mention2.getIds()[0]);
    }
}


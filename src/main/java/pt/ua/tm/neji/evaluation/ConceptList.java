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

package pt.ua.tm.neji.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents the list of recognized concepts.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class ConceptList extends ArrayList<Concept> {

    private boolean identifierMatch(final Concept gold, final Concept silver, final IdentifierMatch identifierMatch) {
        switch (identifierMatch) {
            case NONE:
                return true;
            case EXACT:
//                return (gold.getIdentifiers().size() == silver.getIdentifiers().size()) &&
//                        gold.getIdentifiers().equals(silver.getIdentifiers());
                if (gold.getIdentifiers().size() >= 1 && silver.getIdentifiers().size() >= 1 &&
                        gold.getIdentifiers().equals(silver.getIdentifiers())) {
                    return true;
                } else {
                    return false;
                }

            case CONTAIN:
                return containsAnyIdentifier(gold.getIdentifiers(), silver.getIdentifiers());
            default:
                throw new RuntimeException("The provided identifier match is not valid.");
        }
    }

    private boolean containsAnyIdentifier(List<String> list1, List<String> list2) {
        for (String id1 : list1) {
            for (String id2 : list2) {
                if (id1.equals(id2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsExact(final Concept t2, final IdentifierMatch identifierMatch) {
        for (Concept t1 : this) {
            if (t1.getEntity().equals(t2.getEntity()) && t1.getStart() == t2.getStart() && t1.getEnd() == t2.getEnd()) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }
        }
        return false;
    }

    public boolean containsShared(final Concept t2, final IdentifierMatch identifierMatch) {
        for (Concept t1 : this) {
            if (t1.getEntity().equals(t2.getEntity()) &&
                    (t1.getStart() == t2.getStart() || t1.getEnd() == t2.getEnd())) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }
        }
        return false;
    }

    public boolean containsLeft(final Concept t2, final IdentifierMatch identifierMatch) {
        for (Concept t1 : this) {
            if (t1.getEntity().equals(t2.getEntity()) && t1.getStart() == t2.getStart()) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }
        }
        return false;
    }

    public boolean containsRight(final Concept t2, final IdentifierMatch identifierMatch) {
        for (Concept t1 : this) {
            if (t1.getEntity().equals(t2.getEntity()) && t1.getEnd() == t2.getEnd()) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }
        }
        return false;
    }

    public boolean containsSubspan(final Concept t2, final IdentifierMatch identifierMatch) {
        for (Concept t1 : this) {
            if (t1.getEntity().equals(t2.getEntity()) && t2.getStart() >= t1.getStart() && t2.getEnd() <= t1.getEnd()) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }
            if (t1.getEntity().equals(t2.getEntity()) && t1.getStart() >= t2.getStart() && t1.getEnd() <= t2.getEnd()) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }
        }
        return false;
    }

    public boolean containsOverlap(final Concept t2, final IdentifierMatch identifierMatch) {
        for (Concept t1 : this) {

            if (!t1.getEntity().equals(t2.getEntity())) {
                continue;
            }
//
//            // Exact
//            if (t1.getStart() == t2.getStart() && t1.getEnd() == t2.getEnd()) {
//                return true;
//            }
//
//            // Subspan
//            if (t2.getStart() >= t1.getStart() && t2.getEnd() <= t1.getEnd()) {
//                return true;
//            }
//            if (t1.getStart() >= t2.getStart() && t1.getEnd() <= t2.getEnd()) {
//                return true;
//            }
//
//            // Shared
//            if (t1.getStart() == t2.getStart() || t1.getEnd() == t2.getEnd()) {
//                return true;
//            }
//
//            // Intersection
//            if (t1.getStart() >= t2.getStart() && t1.getStart() <= t2.getEnd() && t1.getEnd() > t2.getEnd()) {
//                return true;
//            }
//            if (t1.getEnd() >= t2.getStart() && t1.getEnd() <= t2.getEnd() && t1.getStart() < t2.getStart()) {
//                return true;
//            }
//            if (t2.getStart() >= t1.getStart() && t2.getStart() <= t1.getEnd() && t2.getEnd() > t1.getEnd()) {
//                return true;
//            }
//            if (t2.getEnd() >= t1.getStart() && t2.getEnd() <= t1.getEnd() && t2.getStart() < t1.getStart()) {
//                return true;
//            }


            // Good one
//            if (t1.getStart() >= t2.getStart() && t1.getStart() <= t2.getEnd()) {
//                return true;
//            }
//            if (t1.getEnd() >= t2.getStart() && t1.getEnd() <= t2.getEnd()) {
//                return true;
//            }
//
//            if (t2.getStart() >= t1.getStart() && t2.getStart() <= t1.getEnd()) {
//                return true;
//            }
//            if (t2.getEnd() >= t1.getStart() && t2.getEnd() <= t1.getEnd()) {
//                return true;
//            }

            // From COCOA
            if (t1.getStart() == t2.getStart() && t1.getEnd() == t2.getEnd()) {
//                return true;
                return identifierMatch(t1, t2, identifierMatch);
            } else if (t1.getStart() == t2.getStart() || t1.getEnd() == t2.getEnd()) {
                //                return true;
                return identifierMatch(t1, t2, identifierMatch);
            } else if (t1.getStart() > t2.getStart() && t1.getEnd() < t2.getEnd()) {
                //                return true;
                return identifierMatch(t1, t2, identifierMatch);
            } else if (t1.getStart() < t2.getStart() && t1.getEnd() > t2.getEnd()) {
                //                return true;
                return identifierMatch(t1, t2, identifierMatch);
            } else if (t1.getStart() > t2.getStart() && t1.getStart() < t2.getEnd()) {
                //                return true;
                return identifierMatch(t1, t2, identifierMatch);
            } else if (t1.getEnd() > t2.getStart() && t1.getEnd() < t2.getEnd()) {
                //                return true;
                return identifierMatch(t1, t2, identifierMatch);
            }

        }
        return false;
    }
}

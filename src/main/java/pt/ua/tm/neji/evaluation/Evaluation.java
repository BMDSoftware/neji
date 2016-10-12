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

/**
 * Class that stores and iterates common evaluation variables.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 2.0
 * @since 1.0
 */
public class Evaluation {

    private int tp, fp, fn;

    public Evaluation() {
        this.tp = 0;
        this.fp = 0;
        this.fn = 0;
    }

    public int getTP() {
        return tp;
    }

    public int getFP() {
        return fp;
    }

    public int getFN() {
        return fn;
    }

    public void setTP(int tp) {
        this.tp = tp;
    }

    public void setFP(int fp) {
        this.fp = fp;
    }

    public void setFN(int fn) {
        this.fn = fn;
    }

    public void addTP() {
        this.tp++;
    }

    public void addFP() {
        this.fp++;
    }

    public void addFN() {
        this.fn++;
    }

    public double getPrecision() {
        if (tp == 0 && fp == 0) {
            return 0.0;
        }
        return ((double) (tp) / (double) (tp + fp));
    }

    public double getRecall() {
        if (tp == 0 && fn == 0) {
            return 0.0;
        }
        return ((double) (tp) / (double) (tp + fn));
    }

    public double getF1() {
        double p = getPrecision();
        double r = getRecall();

        if (p == 0 && r == 0) {
            return 0.0;
        }

        return 2.0 * ((p * r) / (p + r));
    }

}

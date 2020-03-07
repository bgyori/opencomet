/**
* CometConfiguration.java
* Created in 2019 by Steven Downs
*
* CometConfiguration.java defines values defined by the user and passed to CometAnalyzer.java.
* This class is a wrapper for the cometOptions which already existed in CometAnalyzer.java and was moved here
* so that the addition of the mininum area threshold could be added.
*
* This plugin is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 
* as published by the Free Software Foundation.
*
* This work is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* When you use this plugin for your work, please cite
* Gyori BM, Venkatachalam G, et al. OpenComet: An automated tool for 
* comet assay image analysis
*
* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
public class CometConfiguration {
    public static int COMETFIND_BGCORRECT = 1;
    public static int HEADFIND_AUTO = 2;
    public static int HEADFIND_PROFILE = 4;
    public static int HEADFIND_BRIGHTEST = 8;
    private int cometOptions;
    private int minAreaThreshold;

    public CometConfiguration(int cometOptions) {
        SetCometBooleanFlags(cometOptions);
        SetMinimumAreaThreshold(0);
    }
    public CometConfiguration(int cometOptions, int minAreaThreshold) {
        SetCometBooleanFlags(cometOptions);
        SetMinimumAreaThreshold(minAreaThreshold);
    }
    public int GetCometBooleanFlags() {
        return cometOptions;
    }
    public void SetCometBooleanFlags(int cometOptions) {
        this.cometOptions = cometOptions;
    }
    public int GetMinimumAreaThreshold() {
        return minAreaThreshold;
    }
    public void SetMinimumAreaThreshold(int minAreaThreshold) {
        this.minAreaThreshold = minAreaThreshold;
    }
}
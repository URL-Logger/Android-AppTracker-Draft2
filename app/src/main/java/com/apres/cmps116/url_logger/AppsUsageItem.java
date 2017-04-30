package com.apres.cmps116.url_logger;

/**
 * Created by cedriclinares on 3/7/17.
 */

import java.text.Collator;
import java.util.Comparator;

public class AppsUsageItem {
    public String pkgName;
    public String appName;
    public long lastStartup;
    public String lastStartupStr;
    public long fgTime;
    public long firsttime;
    public long lastime;
    public long currenttime;
    public int mLaunchCount;

    public String userid;

    public int getlaunch() {
        return mLaunchCount;
    }


    public String getpackagename() {
        return appName ;
    }
    public long getfirsttime() {
        return firsttime;
    }

    public long getlasttime() {
        return lastime;
    }

    public long getlastused() {
        return lastStartup;
    }

    public long getforeground() {
        return fgTime;
    }



    public static class AppNameComparator implements Comparator<AppsUsageItem> {
        private Collator mCollator = Collator.getInstance();

        @Override
        public int compare(AppsUsageItem lhs, AppsUsageItem rhs) {
            return mCollator.compare(lhs.appName, rhs.appName);
        }
    }

    public static class LastStartupComparator implements Comparator<AppsUsageItem> {
        @Override
        public int compare(AppsUsageItem lhs, AppsUsageItem rhs) {
            if (lhs.lastStartup < rhs.lastStartup) {
                return 1;
            } else if (lhs.lastStartup > rhs.lastStartup) {
                return -1;
            }
            return 0;
        }
    }

    public static class FgTimeComparator implements Comparator<AppsUsageItem> {
        @Override
        public int compare(AppsUsageItem lhs, AppsUsageItem rhs) {
            if (lhs.fgTime < rhs.fgTime) {
                return 1;
            } else if (lhs.fgTime > rhs.fgTime) {
                return -1;
            }
            return 0;
        }
    }
}

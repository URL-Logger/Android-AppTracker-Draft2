package com.apres.cmps116.url_logger;

import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebView;

/**
 * Created by bereket on 2/20/17.
 */

public class Privacypolicy extends Activity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        String htmlText = "%s";
        String privacydata =
                "\n" +
                        "<p>Last updated: (add date)</p>\n" +
                        "\n" +
                        "<p>My Company (change this) (\"us\", \"we\", or \"our\") operates http://www.mysite.com (change this) (the \"Site\"). This page informs you of our policies regarding the collection, use and disclosure of Personal Information we receive from users of the Site.</p>\n" +
                        "\n" +
                        "We use your Personal Information only for providing and improving the Site. By using the Site, you agree to the collection and use of information in accordance with this policy.</p>\n" +
                        "\n" +
                        "<b>Information Collection And Use</b>\n" +
                        "\n" +
                        "<p>While using our Site, we may ask you to provide us with certain personally identifiable information that can be used to contact or identify you. Personally identifiable information may include, but is not limited to your name (\"Personal Information\").</p>\n" +
                        "\n" +
                        "<b>Log Data</b>\n" +
                        "\n" +
                        "<p>Like many site operators, we collect information that your browser sends whenever you visit our Site (\"Log Data\").</p>\n" +
                        "\n" +
                        "<p>This Log Data may include information such as your computer's Internet Protocol (\"IP\") address, browser type, browser version, the pages of our Site that you visit, the time and date of your visit, the time spent on those pages and other statistics.</p>\n" +
                        "\n" +
                        "<p>In addition, we may use third party services such as Google Analytics that collect, monitor and analyze this ...</p>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<b>Communications</b>\n" +
                        "\n" +
                        "<p>We may use your Personal Information to contact you with newsletters, marketing or promotional materials and other information that ...</p>\n" +
                        "\n" +
                        "\n" +
                        "<b>Cookies</b>\n" +
                        "\n" +
                        "<p>Cookies are files with small amount of data, which may include an anonymous unique identifier. Cookies are sent to your browser from a web site and stored on your computer's hard drive.</p>\n" +
                        "\n" +
                        "<p>Like many sites, we use \"cookies\" to collect information. You can instruct your browser to refuse all cookies or to indicate when a cookie is being sent. However, if you do not accept cookies, you may not be able to use some portions of our Site.</p>\n" +
                        "\n" +
                        "<b>Security</b>\n" +
                        "\n" +
                        "<p>The security of your Personal Information is important to us, but remember that no method of transmission over the Internet, or method of electronic storage, is 100% secure. While we strive to use commercially acceptable means to protect your Personal Information, we cannot guarantee its absolute security.</p>\n" +
                        "\n" +
                        "<b>Changes To This Privacy Policy</b>\n" +
                        "\n" +
                        "<p>This Privacy Policy is effective as of (add date) and will remain in effect except with respect to any changes in its provisions in the future, which will be in effect immediately after being posted on this page.</p>\n" +
                        "\n" +
                        "<p>We reserve the right to update or change our Privacy Policy at any time and you should check this Privacy Policy periodically. Your continued use of the Service after we post any modifications to the Privacy Policy on this page will constitute your acknowledgment of the modifications and your consent to abide and be bound by the modified Privacy Policy.</p>\n" +
                        "\n" +
                        "<p>If we make any material changes to this Privacy Policy, we will notify you either through the email address you have provided us, or by placing a prominent notice on our website.</p>\n" +
                        "\n" +
                        "<b>Contact Us</b>\n" +
                        "\n" +
                        "<p>If you have any questions about this Privacy Policy, please contact us.</p>\n";

        WebView webView = (WebView) findViewById(R.id.webView1);
        webView.loadData(String.format(htmlText, privacydata), "text/html", "utf-8");
    }
}

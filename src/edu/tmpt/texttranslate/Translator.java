package edu.tmpt.texttranslate;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class Translator {

        private LangCode[] lang_chain_save;
        
        public Translator() {
                lang_chain_save = null;
        }
        
        public boolean hasChain() {
                return lang_chain_save == null;
        }
        
        public LangCode[] getLangChain() {
                return lang_chain_save;
        }

        public String getTranslation(String text, double factor,
                        int max_translations) {
        		text = text.replaceAll("[.\\-!?,]", "");
                String t = text;
                Random r = new Random(text.hashCode());
                ArrayList<LangCode> lang_chain = new ArrayList<LangCode>();
                LangCode prev_lang = LangCode.ENGLISH;
                LangCode next_lang = LangCode.ENGLISH;
                lang_chain.add(next_lang);
                for (int i = 0; i < max_translations; i++) {
                        next_lang = LangCode.values()[r.nextInt(LangCode.values().length)];
                        // System.out.format("%s (%d)%n", next_lang, i);
                        lang_chain.add(next_lang);
                        t = getTranslation(t, prev_lang, next_lang);
                        String eng = getTranslation(t, next_lang, LangCode.ENGLISH);
                        // System.out.println(eng);

                        // compare number of same words to length of original
                        String[] t_words = eng.toLowerCase().split("\\W");
                        String[] orig_words = text.toLowerCase().split("\\W");
                        ArrayList<String> same_words = new ArrayList<String>(
                                        Arrays.asList(orig_words));
                        int num_similar_words = 0;
                        for (String tw : t_words) {
                                String matching_word = null;
                                for (String ow : same_words)
                                        if (tw.equalsIgnoreCase(ow)) {
                                                num_similar_words++;
                                                matching_word = ow;
                                                break;
                                        }
                                if (matching_word != null) {
                                        //System.out.println("found matching word: " + matching_word);
                                        same_words.remove(matching_word);
                                }
                        }
                        double curr_factor = (double) num_similar_words / orig_words.length;
                        // System.out.format("factor: %4.2f (%d / %d)%n", curr_factor,
                        // num_similar_words, orig_words.length);
                        if (curr_factor < factor) {
                                break;
                        }

                        prev_lang = next_lang;

                        if (i == max_translations - 1)
                                System.out.println("Reached max translations");
                }
                lang_chain.add(LangCode.ENGLISH);
                lang_chain_save = lang_chain.toArray(new LangCode[] {});
                return getTranslation(t, next_lang, LangCode.ENGLISH);
        }

        public String getTranslation(String text, int num_translations) {
                return getTranslation(text, genLangChain(num_translations));
        }

        public LangCode[] genLangChain(int len) {
                LangCode[] chain = new LangCode[len];
                Random r = new Random(len * len * len * (int) Math.log(len * len * len));
                for (int i = 0; i < len; i++)
                        chain[i] = i == 0 || i == len - 1 ? LangCode.ENGLISH : LangCode
                                        .values()[r.nextInt(LangCode.values().length)];
                return chain;
        }

        public String getTranslation(String text, LangCode... lang_chain) {
                for (int i = 1; i < lang_chain.length; i++)
                        text = getTranslation(text, lang_chain[i - 1], lang_chain[i]);
                return text;
        }

        public String getTranslation(String text, LangCode from, LangCode to) {
                boolean DEBUG = false;
                try {
                        URL url = new URL(
                                        "http://translate.google.com/translate_a/t?client=t&sl="
                                                        + from.code
                                                        + "&tl="
                                                        + to.code
                                                        + "&hl=en&sc=2&ie=UTF-8&oe=UTF-8&rom=1&ssel=0&tsel=0&q="
                                                        + URLEncoder.encode(text, "UTF-8"));
                        if (DEBUG) {
                                System.out.format("Request: %s%nRequest Headers:%n", url);
                        }

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.addRequestProperty("Accept", "*/*");
                        conn.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
                        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                        conn.addRequestProperty("Connection", "keep-alive");
                        conn.addRequestProperty(
                                        "Cookie",
                                        "PREF=ID=aca1e601c42faa14:U=a34154a3bd35772d:LD=en:TM=1356449077:LM=1377790655:DV=Ej1c9pGD6JcazIBPlrtAGQKanXWVgQI:GM=1:S=kIUqUOkjL1PsvVbq; HSID=AgNNBwdFzmin1zPV6; APISID=7I05t1uyu-BVLntw/AOm2RqsT4K_CZA0It; JSESSIONID=oh1h0ODkHeuOO1mxTbqV6A; NID=67=aj4eHv6byGpkurYXK2JmT0RMAJoMBrMZBmiw8LPd1IPs2bEgXCgMzzAf2hSSvawIQ6ALWhMLoxXFhzIaQWXZhji16oKLqyPqOS7VQSO3c59KmBdT-1M5hcmo5E0VF5XfmnOPoBgKvptfdM6C9Oj2x9FUXn_-SlnivuodnvZuR1FQRsoe3W6k33qI36phftxL1qFv72VxTPKFSCDJ7hXXGa68eiul8qeEFR-dHQ; _ga=GA1.3.601603672.1385500726; SID=DQAAAM4AAAD1ntFtbOdE5RrRBUe7d-rvW_T7MCcAAbltw0u8Efrl8W0i-pFNn-YZ6YmsXGSIGrvjbnBN7Cmx7VWYbY4cnu8dw97jsHp_r7K3RONW2XfZIggncGTzPhQBzwSo1VYyuG4xMmdX2q3PFnUyrVdyfsUA8tzxIqPfdsoHsqpUTzHHE3SrLGtPUKFMBUZN4Xn57myd6mSxfG-DbEOwA9VKL9Ld9tVcQW_FK1gs951WI1t_OfAi_Vmu8nl0XBVIFklcp0unXi3X-F9Z06QpvszDcOV5");
                        conn.addRequestProperty("Host", "translate.google.com");
                        conn.addRequestProperty("Referer", "http://translate.google.com/");
                        conn.addRequestProperty(
                                        "User-Agent",
                                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36");
                        conn.addRequestProperty("X-Chrome-UMA-Enabled", "1");
                        conn.addRequestProperty("X-Chrome-Variations",
                                        "CO21yQEIjbbJAQiltskBCKm2yQEIwbbJAQiehsoBCKGIygEIuYjKAQ==");
                        if (DEBUG) {
                                Map<String, List<String>> req = conn.getRequestProperties();
                                for (String key : req.keySet()) {
                                        System.out.format("%s: %s%n", key, req.get(key));
                                }
                        }

                        if (DEBUG) {
                                System.out.format("%nResponse headers:%n");
                                Map<String, List<String>> header = conn.getHeaderFields();
                                for (String key : header.keySet()) {
                                        System.out.format("%s: %s%n", key, header.get(key));
                                }
                        }

                        conn.connect();
                        GZIPInputStream in = new GZIPInputStream(conn.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in,
                                        "UTF-8"));
                        String result = "";
                        String line;
                        while ((line = r.readLine()) != null) {
                                result += line;
                        }
                        

                        if (DEBUG) {
                                System.out.format("%nResponse:%n%s%n", result);
                        }

                        TranslationTokenizer.TranslationList list = TranslationTokenizer
                                        .parse(result);
                        
                        String trans = "";
                        for (int i = 0; i < list.getList(0).size(); i++)
                                trans += list.getList(0).getList(i).getString(0);
                        System.out.println(trans + " " + to);
                        return trans;
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return null;
        }
}
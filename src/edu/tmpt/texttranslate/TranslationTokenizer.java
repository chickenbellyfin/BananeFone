package edu.tmpt.texttranslate;

import java.util.ArrayList;

public class TranslationTokenizer {

	public static class TranslationList extends ArrayList<Object> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6961204765594471134L;

		private static int curr_id = 0;

		private int id;
		private TranslationList superlist;

		public TranslationList(TranslationList superlist) {
			id = curr_id++;
			this.superlist = superlist;
			if (this.superlist != null)
				this.superlist.add(this);
		}

		public int getID() {
			return id;
		}

		public TranslationList getSuperList() {
			return superlist;
		}

		public TranslationList getList(int i) {
			return (TranslationList) get(i);
		}

		public String getString(int i) {
			return (String) get(i);
		}

		public int getInt(int i) {
			return ((Integer) get(i)).intValue();
		}

		public double getDouble(int i) {
			return ((Double) get(i)).doubleValue();
		}

		@Override
		public String toString() {
			return "{ID: " + id + " " + super.toString()
					+ (superlist == null ? "" : " SID: " + superlist.id) + "}";
		}

	}

	public static TranslationList parse(String data) {
		TranslationList list = null;
		boolean in_quotes = false;
		char c, pc = 0;
		String curr_data = "";
		for (int i = 0; i < data.length(); i++) {
			c = data.charAt(i);
			// System.out.format("%c %s%n", c, list);
			switch (c) {
			case '[':
				list = new TranslationList(list);
				break;
			case ']':
				if (list.getSuperList() != null)
					list = list.getSuperList();
				break;
			case '"':
				if (pc != '\\') {
					in_quotes = !in_quotes;
					if (!in_quotes && curr_data.length() != 0)
						list.add(curr_data);
					curr_data = "";
				}
				break;
			case ',':
				if (curr_data.length() != 0 && list != null) {
					try {
						list.add(Integer.parseInt(curr_data));
					} catch (NumberFormatException e) {
						try {
							list.add(Double.parseDouble(curr_data));
						} catch (NumberFormatException e2) {
							list.add(curr_data);
						}
					}
				}
				curr_data = "";
				break;
			default:
				curr_data += c;
				break;
			}
			pc = c;
		}
		if (curr_data.length() != 0 && list != null)
			list.add(curr_data);
		if (list != null)
			while (list.getSuperList() != null)
				list = list.getSuperList();
		return list;
	}

}

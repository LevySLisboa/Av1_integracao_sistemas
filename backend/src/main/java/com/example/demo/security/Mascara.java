package com.example.demo.security;

public final class Mascara {

	private Mascara() {
	}

	/** joao@exemplo.com -> j***@exemplo.com */
	public static String email(String email) {
		int arroba = email == null ? -1 : email.indexOf('@');
		if (arroba < 0) {
			return "***";
		}
		if (arroba == 0) {
			return "***" + email.substring(arroba);
		}
		return email.charAt(0) + "***" + email.substring(arroba);
	}
}

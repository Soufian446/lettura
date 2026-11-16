package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerArchivio {
    public static void main(String[] args) {
        final int PORTA = 3000;
        File cartella = new File("documenti");

        if (!cartella.exists()) {
            cartella.mkdir();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Server avviato sulla porta " + PORTA + "...");
            System.out.println("Cartella documenti: " + cartella.getAbsolutePath());

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    String richiesta = in.readLine();

                    if (richiesta == null) {
                        continue;
                    }

                    System.out.println("Richiesta ricevuta: [" + richiesta + "]");

                    String richiestaPulita = richiesta.trim();
                    if (richiestaPulita.isEmpty()) {
                        inviaErrore(out, "ERR SYNTAX");
                        continue;
                    }

                    String[] parti = richiestaPulita.split("\\s+");
                    String comando = parti[0];

                    switch (comando) {
                        case "LEGGI":

                            if (parti.length != 2 || !nomeValido(parti[1])) {
                                inviaErrore(out, "ERR SYNTAX");
                                break;
                            }

                            String nomeFile = parti[1];
                            File file = new File(cartella, nomeFile);

                            if (!file.isFile()) {
                                inviaErrore(out, "ERR NOTFOUND");
                                break;
                            }

                            boolean statoInviato = false;
                            try (BufferedReader lettoreFile = new BufferedReader(
                                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

                                out.println("OK FILE");
                                out.println();
                                statoInviato = true;

                                String riga;
                                while ((riga = lettoreFile.readLine()) != null) {
                                    out.println(riga);
                                }
                                System.out.println("File '" + nomeFile + "' inviato con successo.");

                            } catch (IOException e) {
                                System.err.println("Errore lettura file: " + e.getMessage());
                                if (!statoInviato) {
                                    inviaErrore(out, "ERR READ");
                                }
                            }
                            break;

                        default:
                            inviaErrore(out, "ERR UNKNOWNCMD");
                            break;
                    }

                } catch (IOException e) {
                    System.err.println("Errore comunicazione client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Errore avvio server: " + e.getMessage());
        }
    }

    private static void inviaErrore(PrintWriter out, String stato) {
        out.println(stato);
        out.println();
        System.out.println("Risposta inviata: " + stato);
    }

    private static boolean nomeValido(String nome) {
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) {
            return false;
        }
        return nome.endsWith(".txt") && nome.length() > 4;
    }
}
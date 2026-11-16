package com.example;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientArchivio {
    public static void main(String[] args) {
        final String HOST = "localhost";
        final int PORTA = 3000;
        Scanner tastiera = new Scanner(System.in);

        System.out.println("=== Archivio di Classe ===");

        while (true) {
            System.out.print("\nInserisci il nome del file da leggere (oppure 'esci' per terminare): ");
            String nomeFile = tastiera.nextLine().trim();

            if ("esci".equalsIgnoreCase(nomeFile)) {
                System.out.println("Chiusura del client.");
                break;
            }

            if (nomeFile.isEmpty()) {
                System.out.println("Inserisci un nome valido.");
                continue;
            }

            // Apre una nuova connessione per ogni richiesta
            try (Socket socket = new Socket(HOST, PORTA);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                // Invio richiesta
                out.println("LEGGI " + nomeFile);

                // Lettura riga di stato
                String stato = in.readLine();
                if (stato == null) {
                    System.out.println("Errore: risposta incompleta dal server (stato mancante).");
                    continue;
                }

                // Lettura riga vuota di separazione
                String separatore = in.readLine();
                if (separatore == null) {
                    System.out.println("Errore: risposta incompleta dal server (separatore mancante).");
                    continue;
                }
                if (!separatore.isEmpty()) {
                    System.out.println("Errore: risposta non conforme al protocollo (separatore non vuoto).");
                    continue;
                }

                // Gestione dell'esito
                if ("OK FILE".equals(stato)) {
                    System.out.println("--- Contenuto di " + nomeFile + " ---");
                    String riga;
                    boolean vuoto = true;
                    while ((riga = in.readLine()) != null) {
                        System.out.println(riga);
                        vuoto = false;
                    }
                    if (vuoto) {
                        System.out.println("(Il file è vuoto)");
                    }
                    System.out.println("----------------------------------");
                } else {
                    mostraMessaggioErrore(stato);
                }

            } catch (ConnectException e) {
                System.out.println("Impossibile connettersi al server. Assicurati che sia avviato.");
            } catch (IOException e) {
                System.out.println("Errore di rete: " + e.getMessage());
            }
        }

        tastiera.close();
    }

    private static void mostraMessaggioErrore(String stato) {
        switch (stato) {
            case "ERR NOTFOUND":
                System.out.println("Errore: il file richiesto non esiste sul server.");
                break;
            case "ERR SYNTAX":
                System.out.println("Errore: sintassi non valida o nome file non ammesso (usa solo file .txt semplici).");
                break;
            case "ERR UNKNOWNCMD":
                System.out.println("Errore: comando non riconosciuto dal server.");
                break;
            case "ERR READ":
                System.out.println("Errore: impossibile leggere il file sul server.");
                break;
            default:
                System.out.println("Errore sconosciuto dal server: " + stato);
                break;
        }
    }
}


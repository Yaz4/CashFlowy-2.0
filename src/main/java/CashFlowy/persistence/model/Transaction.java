package CashFlowy.persistence.model;

import java.time.LocalDate;

public class Transaction {
    private Long id;
    private String categoria;
    private String descrizione;
    private double importo;
    private LocalDate data;
    private String tipo; // "Entrata" o "Uscita"

    public Transaction(Long id, String categoria, String descrizione, double importo, LocalDate data, String tipo) {
        this.id = id;
        this.categoria = categoria;
        this.descrizione = descrizione;
        this.importo = importo;
        this.data = data;
        this.tipo = tipo;
    }

    public Transaction(String categoria, String descrizione, double importo, LocalDate data, String tipo) {
        this.categoria = categoria;
        this.descrizione = descrizione;
        this.importo = importo;
        this.data = data;
        this.tipo= tipo;
    }

    //getters
    public Long getId() {
        return id;
    }
    public String getCategoria() {return categoria; }
    public String getDescrizione() { return descrizione; }
    public double getImporto() { return importo; }
    public LocalDate getData() {return data;}
    public String getTipo() { return tipo; }


    //setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategoria(String categoria) {   this.categoria = categoria; }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}


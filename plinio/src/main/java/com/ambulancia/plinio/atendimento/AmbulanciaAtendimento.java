package com.ambulancia.plinio.atendimento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class AmbulanciaAtendimento {

    private final Set<String> bairros = new HashSet<>();
    private final Map<String, Set<String>> grafo = new HashMap<>();
    private final List<HospitalCadastro> hospitais = new ArrayList<>();

    public void adicionarBairro(String nome) {
        Objects.requireNonNull(nome, "bairro");
        bairros.add(nome);
        grafo.putIfAbsent(nome, new TreeSet<>());
    }

    public void conectarBairros(String a, String b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        if (!bairros.contains(a) || !bairros.contains(b)) {
            throw new IllegalArgumentException("Bairros precisam existir: " + a + ", " + b);
        }
        grafo.computeIfAbsent(a, k -> new TreeSet<>()).add(b);
        grafo.computeIfAbsent(b, k -> new TreeSet<>()).add(a);
    }

    public void adicionarHospital(String nomeHospital, String bairro, int totalLeitos, int leitosOcupados) {
        Objects.requireNonNull(nomeHospital);
        Objects.requireNonNull(bairro);
        if (!bairros.contains(bairro)) {
            throw new IllegalArgumentException("Bairro inexistente: " + bairro);
        }
        if (totalLeitos < 0 || leitosOcupados < 0 || leitosOcupados > totalLeitos) {
            throw new IllegalArgumentException("Leitos inválidos para " + nomeHospital);
        }
        hospitais.add(new HospitalCadastro(nomeHospital, bairro, totalLeitos, leitosOcupados));
    }

    public String encontrarHospital(String bairroOrigem) {
        if (!bairros.contains(bairroOrigem)) {
            throw new IllegalArgumentException("Bairro inexistente: " + bairroOrigem);
        }

        String melhorNoBairro = melhorHospitalNoBairro(bairroOrigem);
        if (melhorNoBairro != null) {
            return melhorNoBairro;
        }

        Set<String> visitados = new HashSet<>();
        List<String> camada = new ArrayList<>();
        camada.add(bairroOrigem);
        visitados.add(bairroOrigem);

        while (!camada.isEmpty()) {
            List<String> proxima = new ArrayList<>();

            for (String v : new TreeSet<>(camada)) {
                for (String vizinho : grafo.getOrDefault(v, Collections.emptySet())) {
                    if (visitados.add(vizinho)) {
                        proxima.add(vizinho);
                    }
                }
            }

            String melhorHospital = null;
            for (String v : new TreeSet<>(proxima)) {
                String h = melhorHospitalNoBairro(v);
                if (h != null && (melhorHospital == null || h.compareTo(melhorHospital) < 0)) {
                    melhorHospital = h;
                }
            }
            if (melhorHospital != null) {
                return melhorHospital;
            }

            camada = proxima;
        }

        return "";
    }

    private String melhorHospitalNoBairro(String bairro) {
        TreeSet<String> nomes = new TreeSet<>();
        for (HospitalCadastro h : hospitais) {
            if (h.bairro().equals(bairro) && h.leitosOcupados() < h.totalLeitos()) {
                nomes.add(h.nome());
            }
        }
        return nomes.isEmpty() ? null : nomes.first();
    }

    private record HospitalCadastro(String nome, String bairro, int totalLeitos, int leitosOcupados) {
    }
}

package com.ambulancia.plinio.atendimento;

public final class AmbulanciaAtendimentoMain {

    private AmbulanciaAtendimentoMain() {
    }

    public static void main(String[] args) {
        AmbulanciaAtendimento atendimento = new AmbulanciaAtendimento();

        String[] bairros = {
                "Pinheiros", "Vila Madalena", "Butantã", "Lapa", "Perdizes",
                "Barra Funda", "Alto de Pinheiros", "Vila Leopoldina", "Jaguaré",
                "Vila Sônia", "Morumbi", "Rio Pequeno", "Pompéia", "Jardim Paulista", "Itaim Bibi"
        };
        for (String bairro : bairros) {
            atendimento.adicionarBairro(bairro);
        }

        atendimento.conectarBairros("Pinheiros", "Vila Madalena");
        atendimento.conectarBairros("Pinheiros", "Butantã");
        atendimento.conectarBairros("Pinheiros", "Alto de Pinheiros");
        atendimento.conectarBairros("Vila Madalena", "Perdizes");
        atendimento.conectarBairros("Perdizes", "Lapa");
        atendimento.conectarBairros("Lapa", "Barra Funda");
        atendimento.conectarBairros("Butantã", "Jaguaré");
        atendimento.conectarBairros("Jaguaré", "Vila Leopoldina");
        atendimento.conectarBairros("Butantã", "Vila Sônia");
        atendimento.conectarBairros("Vila Sônia", "Morumbi");
        atendimento.conectarBairros("Morumbi", "Rio Pequeno");
        atendimento.conectarBairros("Perdizes", "Pompéia");
        atendimento.conectarBairros("Pinheiros", "Jardim Paulista");
        atendimento.conectarBairros("Jardim Paulista", "Itaim Bibi");

        atendimento.adicionarHospital("Hospital das Clínicas", "Pinheiros", 50, 50);
        atendimento.adicionarHospital("UPA Vila Madalena", "Vila Madalena", 20, 20);
        atendimento.adicionarHospital("Hospital Universitário USP", "Butantã", 60, 45);
        atendimento.adicionarHospital("UPA Lapa", "Lapa", 30, 30);
        atendimento.adicionarHospital("Hospital São Camilo", "Perdizes", 40, 39);
        atendimento.adicionarHospital("Santa Casa Barra Funda", "Barra Funda", 35, 35);
        atendimento.adicionarHospital("UPA Alto de Pinheiros", "Alto de Pinheiros", 25, 25);
        atendimento.adicionarHospital("Hospital Vila Penteado", "Vila Leopoldina", 30, 30);
        atendimento.adicionarHospital("UPA Jaguaré", "Jaguaré", 20, 18);
        atendimento.adicionarHospital("Hospital Leforte", "Vila Sônia", 45, 44);
        atendimento.adicionarHospital("Albert Einstein Morumbi", "Morumbi", 50, 50);
        atendimento.adicionarHospital("UPA Rio Pequeno", "Rio Pequeno", 30, 28);
        atendimento.adicionarHospital("São Camilo Pompéia", "Pompéia", 40, 40);
        atendimento.adicionarHospital("Sírio-Libanês Jardins", "Jardim Paulista", 60, 60);
        atendimento.adicionarHospital("São Luiz Itaim", "Itaim Bibi", 55, 55);

        String[] bairrosTeste = {
                "Pinheiros", "Vila Madalena", "Butantã", "Lapa", "Perdizes",
                "Jaguaré", "Vila Sônia", "Rio Pequeno", "Pompéia", "Alto de Pinheiros"
        };

        String[] esperados = {
                "Hospital Universitário USP",
                "Hospital São Camilo",
                "Hospital Universitário USP",
                "Hospital São Camilo",
                "Hospital São Camilo",
                "UPA Jaguaré",
                "Hospital Leforte",
                "UPA Rio Pequeno",
                "Hospital São Camilo",
                "Hospital Universitário USP"
        };

        System.out.println("Testes de Ambulancia:");
        for (int i = 0; i < bairrosTeste.length; i++) {
            String resultado = atendimento.encontrarHospital(bairrosTeste[i]);
            boolean passou = resultado.equals(esperados[i]);
            System.out.printf("Solicitacao em %-18s -> %-30s [%s]%n",
                    bairrosTeste[i], resultado, passou ? "OK" : "FALHOU");
        }
    }
}

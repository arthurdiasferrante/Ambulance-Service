package com.ambulancia.plinio.routing;

public final class NeighborhoodHospitalRouterDemo {

    private NeighborhoodHospitalRouterDemo() {}

    public static void main(String[] args) {
        NeighborhoodHospitalRouter router = new NeighborhoodHospitalRouter();

        String[] neighborhoods = {
                "Pinheiros", "Vila Madalena", "Butantã", "Lapa", "Perdizes",
                "Barra Funda", "Alto de Pinheiros", "Vila Leopoldina", "Jaguaré",
                "Vila Sônia", "Morumbi", "Rio Pequeno", "Pompéia", "Jardim Paulista", "Itaim Bibi"
        };
        for (String n : neighborhoods) {
            router.addNeighborhood(n);
        }

        router.connectNeighborhoods("Pinheiros", "Vila Madalena");
        router.connectNeighborhoods("Pinheiros", "Butantã");
        router.connectNeighborhoods("Pinheiros", "Alto de Pinheiros");
        router.connectNeighborhoods("Vila Madalena", "Perdizes");
        router.connectNeighborhoods("Perdizes", "Lapa");
        router.connectNeighborhoods("Lapa", "Barra Funda");
        router.connectNeighborhoods("Butantã", "Jaguaré");
        router.connectNeighborhoods("Jaguaré", "Vila Leopoldina");
        router.connectNeighborhoods("Butantã", "Vila Sônia");
        router.connectNeighborhoods("Vila Sônia", "Morumbi");
        router.connectNeighborhoods("Morumbi", "Rio Pequeno");
        router.connectNeighborhoods("Perdizes", "Pompéia");
        router.connectNeighborhoods("Pinheiros", "Jardim Paulista");
        router.connectNeighborhoods("Jardim Paulista", "Itaim Bibi");

        router.addHospital("Hospital das Clínicas", "Pinheiros", 50, 50);
        router.addHospital("UPA Vila Madalena", "Vila Madalena", 20, 20);
        router.addHospital("Hospital Universitário USP", "Butantã", 60, 45);
        router.addHospital("UPA Lapa", "Lapa", 30, 30);
        router.addHospital("Hospital São Camilo", "Perdizes", 40, 39);
        router.addHospital("Santa Casa Barra Funda", "Barra Funda", 35, 35);
        router.addHospital("UPA Alto de Pinheiros", "Alto de Pinheiros", 25, 25);
        router.addHospital("Hospital Vila Penteado", "Vila Leopoldina", 30, 30);
        router.addHospital("UPA Jaguaré", "Jaguaré", 20, 18);
        router.addHospital("Hospital Leforte", "Vila Sônia", 45, 44);
        router.addHospital("Albert Einstein Morumbi", "Morumbi", 50, 50);
        router.addHospital("UPA Rio Pequeno", "Rio Pequeno", 30, 28);
        router.addHospital("São Camilo Pompéia", "Pompéia", 40, 40);
        router.addHospital("Sírio-Libanês Jardins", "Jardim Paulista", 60, 60);
        router.addHospital("São Luiz Itaim", "Itaim Bibi", 55, 55);

        String[] origins = {
                "Pinheiros", "Vila Madalena", "Butantã", "Lapa", "Perdizes",
                "Jaguaré", "Vila Sônia", "Rio Pequeno", "Pompéia", "Alto de Pinheiros"
        };

        String[] expected = {
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

        System.out.println("Ambulance routing demo:");
        for (int i = 0; i < origins.length; i++) {
            String result = router.findHospital(origins[i]);
            boolean pass = result.equals(expected[i]);
            System.out.printf("Request from %-18s -> %-30s [%s]%n", origins[i], result, pass ? "OK" : "FAIL");
        }
    }
}

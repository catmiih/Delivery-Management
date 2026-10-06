# Delivery Management

> Sistema distribuído e orientado a eventos para gestão de entregas e alocação de entregadores.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black?logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database%20per%20Service-blue?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?logo=docker&logoColor=white)

---

## 🎯 Sobre o Projeto

O objetivo do projeto é implementar uma arquitetura de microsserviços desacoplada, orientada pelo **Domain-Driven Design (DDD)** e por **Event-Driven Architecture (EDA)**.

A plataforma gerencia o ciclo completo de vida de uma encomenda: desde o cadastro e cálculo de taxas até a alocação e conclusão da entrega pelos entregadores, utilizando comunicação síncrona (REST) e assíncrona orientada a eventos (Apache Kafka).

---

## 🗺️ Bounded Contexts (DDD)

A solução foi decomposta em contextos delimitados para garantir coesão e independência:

1. **Contexto de Monitoramento de Entregas (`Delivery Microservice`)**
    * **Entidades:** Encomenda, Remetente, Destinatário, Item.
    * **Responsabilidades:** Cadastro da encomenda, precificação, cálculo de frete/taxas e acompanhamento do ciclo de status.
2. **Contexto de Gestão de Entregadores (`Courier Microservice`)**
    * **Entidades:** Entregador, Encomenda (visão operacional da entrega).
    * **Responsabilidades:** Gestão e disponibilidade de entregadores, alocação de corridas e aceite de entrega.
3. **Contexto de Suporte ao Cliente (`Third-Party App`)**
    * **Entidades:** Ticket.
    * **Responsabilidades:** Integração com plataforma externa para resolução de incidentes.

---

## 🏛️ Arquitetura e Comunicação

O ecossistema adota **Database per Service** com bancos de dados isolados e duas formas de comunicação entre serviços:

```text
       ┌────────────────────────┐
       │  Delivery Microservice │
       │    (Spring Boot 3)     │
       └────┬──────────────┬────┘
            │              │
       (REST/Sync)    (Kafka/Async)
            │              │
            V              V
       ┌────────────────────────┐         ┌───────────────────┐
       │  Courier Microservice  │────────>│   Apache Kafka    │
       │    (Spring Boot 3)     │         │ (Event Streaming) │
       └────┬───────────────────┘         └───────────────────┘
            │              │
            V              V
     [(PostgreSQL)]  [(PostgreSQL)]
      delivery_db      courier_db
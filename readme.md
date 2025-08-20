# Simple Spring Boot Multi-Datasource-Async-File-Logging Example



## 📌 Features

- 250825 update, JDK 21, Spring Boot 3.5.4
    - PostgreSQL, SQLite3(mybatis 3.0.5)
- 멀티 데이터소스 상태에서 file write
  - 실제 구현은 사이드 이펙트 여지로 인해 다른 소스에 영향을 끼치지 않기 위해 JDBC Connection과 Statement를 이용하여 데이터를 받아 Map, List, Jackson을 이용하였음
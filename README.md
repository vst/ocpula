# ocpula - OpenCPU RPC Client for JSON Endpoints

*ocpula* provides a tiny Scala library to invoke OpenCPU RPC JSON endpoints.

> **Note** that this is an experimental project. Use at your own risk and sadness.

Usage:

    val response: Try[String] = RPC("http://localhost:8000/ocpu", "stats", "rnorm") ~ "{\"n\": 100}"

``TODO: Provide a complete README``

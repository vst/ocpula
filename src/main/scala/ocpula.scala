/*
 * Copyright (c) 2016 Vehbi Sinan Tunalioglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vsthost.rnd.ocpula

import java.net.{ConnectException, MalformedURLException}

import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._

import scala.util.{Failure, Success, Try}
import scalaj.http._


/**
  * Defines a case class for RPC endoints.
  *
  * @param base The base URL of the RPC endpoint.
  * @param lib  The library name.
  * @param func The function name.
  */
case class RPC (base: String, lib: String, func: String) {
  /**
    * Provides a convenience method for invoking the endpoint with a given set of parameters.
    *
    * @param params JSON formatted parameters.
    * @return A trial return value.
    */
  def ~ (params: String): Try[String] = RPC.invoke(base, lib, func, params)
}


/**
  * Provides the workhorse object for the RPC JSON endpoint invokation.
  */
object RPC {
  /**
    * Defines the base program error exception for the library.x
    */
  abstract class Error(msg: String) extends Exception(msg)

  /**
    * Defines a runtime error exception.
    */
  case class RuntimeError (msg: String) extends Error(msg)

  /**
    * Defines a network error exception
    */
  case class NetworkError (msg: String) extends Error(msg)

  /**
    * Returns the endpoint for a given function of a library.
    */
  def endpoint (base: String, lib: String, func: String): Uri =
    base / "library" / lib / "R" / func / "json" ? ("force" -> "true") & ("null" -> "null")

  /**
    * Prepares the base request.
    */
  def request (base: String, lib: String, func: String, params: String): Try[HttpResponse[String]] =
    Try(Http(endpoint(base, lib, func)).header("Content-Type", "application/json").postData(params).asString)

  /**
    * Parses the response and returns a proper [[Try]] value.
    *
    * @param response The [[HttpResponse]] received from RPC endpoint.
    * @return A [[Try[String]] value of our taste.
    */
  def parseResponse(response: HttpResponse[String]): Try[String] = response match {
    case HttpResponse(b, c, _) if c == 200 => Success(b.trim)
    case HttpResponse(b, c, _) if c == 400 => Failure(RuntimeError(s"Input error: ${b.trim.replace("\n", " ")}"))
    case HttpResponse(_, c, _) if c == 400 => Failure(NetworkError("Endpoint not found."))
    case HttpResponse(_, c, _) if c == 500 => Failure(RuntimeError("Something went wrong internally."))
    case HttpResponse(b, c, _)             => Failure(RuntimeError(s"${b.trim} (Error Code: ${c})"))
  }

  /**
    * Send an RPC call to OpenCPU Server.
    */
  def invoke (base: String, lib: String, func: String, params: String): Try[String] = {
    request(base, lib, func, params) match {
      case Failure(exc: ConnectException)      => Failure(NetworkError(exc.getMessage))
      case Failure(exc: MalformedURLException) => Failure(NetworkError(exc.getMessage))
      case Failure(exc)                        => Failure(exc)
      case Success(response)                   => parseResponse(response)
    }
  }
}

/**
  * Defines an entry point object.
  */
object ocpula {
  /**
    * Defines the version of the project artifact.
    */
  val VERSION: String = "0.0.1-SNAPSHOT"

  /**
    * Provides the main entry point of the application.
    */
  def main(args: Array[String]) = {
    // Print quick output.
    System.err.println(Console.BLUE + s"ocpula v${VERSION} - OpenCPU RPC Client for JSON Endpoint" + Console.RESET)

    // Do we have any arguments?
    if (args.length != 4) {
      System.err.print(Console.RED)
      System.err.println("Usage: ocpula <base-url> <library> <function> <parameters>")
      System.err.print(Console.RESET)
      System.exit(1)
    }

    // Get the response:
    val response: Try[String] = RPC(args(0), args(1), args(2)) ~ args(3)

    // Act accorting to response status:
    response match {
      case Success(retval) => println(retval)
      case Failure(xerror) => {
        System.err.print(Console.RED)
        xerror match {
          case RPC.NetworkError(msg) => System.err.println(s"Network Error: ${msg}")
          case RPC.RuntimeError(msg) => System.err.println(s"Runtime Error: ${msg}")
          case exception: Exception  => System.err.println(s"Unknown Error: ${exception.getMessage}")
        }
        System.err.print(Console.RESET)
        System.exit(1)
      }
    }
  }
}

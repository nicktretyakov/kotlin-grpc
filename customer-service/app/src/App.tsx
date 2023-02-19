import React, { useState, useContext } from "react";
import * as grpcWeb from "grpc-web";
import * as jspb from "google-protobuf";

import { CheckReply, CheckRequest } from "./grpc-web/check_pb";
import { useGrpcRequest } from "./common/useGrpcHook";
import { ClientContext } from "./index";
import logo from "./logo.svg";
import "./App.css";

const App: React.FC = () => {
  const greeterClient = useContext(ClientContext);
  const newCheckRequest = async (request: CheckRequest) => {
    return new Promise<CheckReply>((resolve, reject) => {
      greeterClient.sayCheck(request, {}, (err, response: CheckReply) => {
        if (err) {
          if (err.code !== grpcWeb.StatusCode.OK) {
            reject(err);
            console.log("Error code: " + err.code + ' "' + err.message + '"');
          }
        } else {
          resolve(response);
        }
      });
    });
  };

  const request = new CheckRequest();
  request.setName("Markus");

  const [data, error, loading, refetch] = useGrpcRequest(
    newCheckRequest,
    request,
    []
  );

  const handleClick = () => refetch();

  if (error) {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <div>
            {loading ? (
              <div>Retrying...</div>
            ) : (
              <div>Error: {error.message}</div>
            )}
            <button onClick={handleClick}>Retry</button>
          </div>
        </header>
      </div>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Check, my grpc service message is:{" "}
          {loading ? "loading..." : data.message}
        </p>
        <button onClick={handleClick}>Request again</button>
      </header>
    </div>
  );
};

export default App;

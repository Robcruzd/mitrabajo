import { ApolloClient, InMemoryCache, HttpLink } from "@apollo/client";

const link = new HttpLink({
  uri: "http://localhost:8085/graphql"
});

export const client = new ApolloClient({
  link,
  cache: new InMemoryCache()
});

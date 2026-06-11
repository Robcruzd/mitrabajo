import { useState } from "react";
import { gql } from "@apollo/client";
import { useLazyQuery } from "@apollo/client/react"

const JOBS_BY_KEYWORD = gql`
  query JobsByKeyword($keyword: String!, $page: Int!, $size: Int!) {
    jobsByKeyword(keyword: $keyword, page: $page, size: $size) {
      jobId
      title
      company
      location
      experienceLevel
      score
      url
    }
  }
`;

const JOBS_BY_KEYWORD_LOCATION = gql`
  query JobsByKeywordAndLocation(
    $keyword: String!
    $location: String!
    $page: Int!
    $size: Int!
  ) {
    jobsByKeywordAndLocation(
      keyword: $keyword
      location: $location
      page: $page
      size: $size
    ) {
      jobId
      title
      company
      location
      experienceLevel
      score
      url
    }
  }
`;

export default function JobSearch() {
  const [keyword, setKeyword] = useState("");
  const [location, setLocation] = useState("");

  const [search, { data: data1, loading: loading1 }] =
    useLazyQuery(JOBS_BY_KEYWORD);

  const [searchWithLocation, { data: data2, loading: loading2 }] =
    useLazyQuery(JOBS_BY_KEYWORD_LOCATION);

  const handleSearch = () => {
    search({
      variables: { keyword, page: 0, size: 10 }
    });
  };

  const handleSearchLocation = () => {
    searchWithLocation({
      variables: { keyword, location, page: 0, size: 10 }
    });
  };

  const jobs =
    data2?.jobsByKeywordAndLocation ||
    data1?.jobsByKeyword ||
    [];

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-4xl mx-auto bg-white p-6 rounded-2xl shadow-lg">
        <h1 className="text-3xl font-bold mb-6 text-center">
          🔎 Buscar empleos
        </h1>

        {/* Busqueda simple */}
        <div className="flex gap-4 mb-4">
          <input
            className="flex-1 border p-3 rounded-lg"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Java, Backend..."
          />
          <button
            onClick={handleSearch}
            className="bg-blue-600 text-white px-6 rounded-lg hover:bg-blue-700"
          >
            Buscar
          </button>
        </div>

        {/* Busqueda con ubicación */}
        <div className="flex gap-4 mb-6">
          <input
            className="flex-1 border p-3 rounded-lg"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="Bogotá, Medellín..."
          />
          <button
            onClick={handleSearchLocation}
            className="bg-green-600 text-white px-6 rounded-lg hover:bg-green-700"
          >
            Buscar por ubicación
          </button>
        </div>

        {/* Loading */}
        {(loading1 || loading2) && (
          <p className="text-center text-gray-500">Cargando empleos...</p>
        )}

        {/* Resultados */}
        <div className="grid gap-4">
          {jobs.length === 0 && !loading1 && !loading2 && (
            <p className="text-center text-gray-400">
              No hay resultados aún
            </p>
          )}

          {jobs.map((job) => (
            <div
              key={job.jobId}
              className="border p-4 rounded-xl shadow-sm hover:shadow-md transition"
            >
              <h2 className="text-xl font-semibold">{job.title}</h2>
              <p className="text-gray-600">
                {job.company} • {job.location}
              </p>
              <p className="text-sm text-gray-500">
                Nivel: {job.experienceLevel}
              </p>
              <p className="text-sm text-blue-600">
                Score: {job.score}
              </p>

              <a
                href={job.url}
                target="_blank"
                rel="noreferrer"
                className="inline-block mt-2 text-white bg-black px-4 py-2 rounded-lg"
              >
                Ver oferta
              </a>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

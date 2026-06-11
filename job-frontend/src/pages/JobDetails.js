import { useParams } from "react-router-dom";
import { gql } from "@apollo/client";
import { useQuery } from "@apollo/client/react";

const JOB_DETAIL = gql`
  query Job($id: ID!) {
    job(id: $id) {
      id
      title
      description
      company
      location
    }
  }
`;

export default function JobDetails() {
  const { id } = useParams();
  const { data } = useQuery(JOB_DETAIL, { variables: { id } });

  const job = data?.job;

  if (!job) return <p>Cargando...</p>;

  return (
    <div>
      <h2>{job.title}</h2>
      <p>{job.company}</p>
      <p>{job.location}</p>
      <p>{job.description}</p>
    </div>
  );
}

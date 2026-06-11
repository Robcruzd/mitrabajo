import { useState } from "react";
import { gql } from "@apollo/client";
import { useMutation } from "@apollo/client/react"

const SAVE_PROFILE = gql`
  mutation SaveProfile($input: CandidateInput!) {
    saveProfile(input: $input) {
      id
      name
      email
    }
  }
`;

export default function CandidateProfile() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    skills: ""
  });

  const [saveProfile] = useMutation(SAVE_PROFILE);

  const handleSubmit = async () => {
    await saveProfile({
      variables: {
        input: {
          ...form,
          skills: form.skills.split(",")
        }
      }
    });
    alert("Perfil guardado");
  };

  return (
    <div>
      <h1>Perfil del candidato</h1>

      <input
        placeholder="Nombre"
        onChange={e => setForm({ ...form, name: e.target.value })}
      />

      <input
        placeholder="Email"
        onChange={e => setForm({ ...form, email: e.target.value })}
      />

      <input
        placeholder="skills: java,spring,kafka"
        onChange={e => setForm({ ...form, skills: e.target.value })}
      />

      <button onClick={handleSubmit}>Guardar</button>
    </div>
  );
}

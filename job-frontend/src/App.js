import { BrowserRouter, Routes, Route } from "react-router-dom";
import JobSearch from "./pages/JobSearch";
import JobDetails from "./pages/JobDetails";
import CandidateProfile from "./pages/CandidateProfile";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<JobSearch />} />
        <Route path="/job/:id" element={<JobDetails />} />
        <Route path="/profile" element={<CandidateProfile />} />
      </Routes>
    </BrowserRouter>
  );
}

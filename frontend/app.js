document.addEventListener("DOMContentLoaded", () => {
  console.log("app.js loaded");

  const resumeForm = document.getElementById("resumeForm");
  const resumeFile = document.getElementById("resumeFile");
  const uploadBox = document.getElementById("uploadBox");
  const fileName = document.getElementById("fileName");
  const fileMeta = document.getElementById("fileMeta");
  const analyzeBtn = document.getElementById("analyzeBtn");
  const jobDescriptionInput = document.getElementById("jobDescription");
  const resultBox = document.getElementById("resultBox");

  if (!resumeForm || !resumeFile || !uploadBox || !fileName || !fileMeta || !analyzeBtn || !jobDescriptionInput || !resultBox) {
    console.error("One or more DOM elements were not found.");
    return;
  }

  uploadBox.addEventListener("click", () => {
    resumeFile.click();
  });

  resumeFile.addEventListener("change", () => {
    const file = resumeFile.files[0];
    if (!file) return;
    fileName.textContent = file.name;
    fileMeta.textContent = Math.round(file.size / 1024) + " KB";
  });

  resumeForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    console.log("Form submitted");

    const file = resumeFile.files[0];
    const jobDescription = jobDescriptionInput.value.trim();

    if (!file) {
      alert("Please upload a resume PDF.");
      return;
    }

    if (!jobDescription) {
      alert("Please enter a job description.");
      return;
    }

    analyzeBtn.disabled = true;
    analyzeBtn.textContent = "Analyzing...";
    resultBox.innerHTML = "<p>Analyzing your resume, please wait...</p>";

    try {
      const uploadFormData = new FormData();
      uploadFormData.append("userId", "1");
      uploadFormData.append("file", file);

      const uploadResponse = await fetch("http://localhost:8080/api/resumes/upload", {
        method: "POST",
        body: uploadFormData
      });

      console.log("Upload status:", uploadResponse.status);

      if (!uploadResponse.ok) {
        const errText = await uploadResponse.text();
        throw new Error("Upload failed: " + errText);
      }

      const resume = await uploadResponse.json();
      console.log("Uploaded resume:", resume);

      const analyzeFormData = new FormData();
      analyzeFormData.append("resumeId", resume.id);
      analyzeFormData.append("jobDescription", jobDescription);
      analyzeFormData.append("file", file);

      const analyzeResponse = await fetch("http://localhost:8080/api/resumes/analyze", {
        method: "POST",
        body: analyzeFormData
      });

      console.log("Analyze status:", analyzeResponse.status);

      if (!analyzeResponse.ok) {
        const errText = await analyzeResponse.text();
        throw new Error("Analysis failed: " + errText);
      }

      const result = await analyzeResponse.json();
      console.log("Analysis result:", result);

      const atsScore = result.atsScore ?? 0;
      const matchedSkills = Array.isArray(result.matchedSkills) ? result.matchedSkills : [];
      const missingSkills = Array.isArray(result.missingSkills) ? result.missingSkills : [];
      const suggestions = Array.isArray(result.suggestions) ? result.suggestions : [];

      resultBox.innerHTML = `
        <h2>ATS Score: ${atsScore}%</h2>

        <h3>✅ Matched Skills</h3>
        <ul>
          ${matchedSkills.length ? matchedSkills.map(s => `<li>${s}</li>`).join("") : "<li>None found</li>"}
        </ul>

        <h3>❌ Missing Skills</h3>
        <ul>
          ${missingSkills.length ? missingSkills.map(s => `<li>${s}</li>`).join("") : "<li>None — great match!</li>"}
        </ul>

        <h3>💡 Suggestions</h3>
        <ul>
          ${suggestions.length ? suggestions.map(s => `<li>${s}</li>`).join("") : "<li>No suggestions</li>"}
        </ul>
      `;
    } catch (error) {
      console.error("Error:", error);
      resultBox.innerHTML = `<p style="color:red;">Something went wrong: ${error.message}</p>`;
    } finally {
      analyzeBtn.disabled = false;
      analyzeBtn.textContent = "Analyze With AI";
    }
  });
});
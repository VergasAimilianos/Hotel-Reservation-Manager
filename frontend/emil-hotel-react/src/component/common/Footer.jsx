const FooterComponent = () => {
  return (
    <footer>
      <span className="my-footer">
        Ionio Gastronomy Suites | All Rights Reserved &copy;{" "}
        {new Date().getFullYear()}
      </span>
    </footer>
  );
};

export default FooterComponent;
